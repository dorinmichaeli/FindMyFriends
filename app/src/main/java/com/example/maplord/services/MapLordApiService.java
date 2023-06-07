package com.example.maplord.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.maplord.BuildConfig;
import com.example.maplord.api.MapLordApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.Point;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapLordApiService {
  private final MapLordApi api;
  private List<MapLordApi.MarkerInfo> preExistingMarkers = null;

  // Dependencies.
  private final UserService userService;
  private final DialogService dialogService;

  public MapLordApiService(String maplordApiUrl, UserService userService, DialogService dialogService) {
    this.api = createMapLordApi(maplordApiUrl);
    this.userService = userService;
    this.dialogService = dialogService;
  }

  private MapLordApi createMapLordApi(String maplordApiUrl) {
    Gson gson = new GsonBuilder()
      .create();

    var builder = new OkHttpClient.Builder();

    if (BuildConfig.DEBUG) {
      // DANGEROUS - Disable SSL certificate authority validation, this is only used during development.
      dangerousDisableSslCertificateAuthorityValidation(builder);
    }

    OkHttpClient client = builder
      // ** MIDDLEWARE **
      // Add the auth token to every request.
      .addInterceptor(chain -> {
        // Get the auth token from the user service. If the token needs to be
        // refreshed by sending a request to the Firebase API, this will block
        // until the refresh is complete. Blocking this thread is *probably*
        // fine, since this interceptor is running on a background thread.
        String authToken = userService.getAuthTokenSync();

        // Create a new request with the auth token in the header.
        //
        // NOTE:
        //   OkHttpClient3 Request objects are immutable, that's why we have
        //   to create a new one instead of modifying the existing one.
        var request = chain.request()
          .newBuilder()
          .addHeader("Auth", authToken)
          .build();

        return chain.proceed(request);
      })
      .build();

    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(maplordApiUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build();

    MapLordApi api = retrofit.create(MapLordApi.class);

    return api;
  }

  public LiveData<Boolean> updatePreExistingMarkers() {
    var data = new MutableLiveData<>(false);

    Call<List<MapLordApi.MarkerInfo>> call = api.listAllMarkers();
    resolveCall(call, (call1, response, err) -> {
      if (err != null) {
        dialogService.fatalError("Error while sending list markers request: " + err);
        return;
      }

      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          dialogService.fatalError("Error while receiving list markers response: " + errBody);
        }
        return;
      }

      List<MapLordApi.MarkerInfo> markerList = response.body();
      assert markerList != null;
      preExistingMarkers = markerList;
      data.setValue(true);
    });

    return data;
  }

  public List<MapLordApi.MarkerInfo> getPreExistingMarkers() {
    assert preExistingMarkers != null;
    return preExistingMarkers;
  }

  public LiveData<MapLordApi.MarkerInfo> apiCreateMarker(Point point) {
    MutableLiveData<MapLordApi.MarkerInfo> data = new MutableLiveData<>();

    MapLordApi.MarkerCreationRequest creationRequest
      = new MapLordApi.MarkerCreationRequest(point);
    Call<MapLordApi.MarkerInfo> call = api.createMarker(creationRequest);
    resolveCall(call, (call1, response, err) -> {
      if (err != null) {
        dialogService.fatalError("Error while sending create marker request: " + err);
        return;
      }

      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          dialogService.fatalError("Error while receiving create marker response: " + errBody);
        }
        return;
      }

      MapLordApi.MarkerInfo createdMarkerInfo = response.body();
      assert createdMarkerInfo != null;
      data.setValue(createdMarkerInfo);
    });

    return data;
  }

  public LiveData<MapLordApi.MarkerDeletionResult> apiDeleteMarker(MapLordApi.MarkerInfo markerInfo) {
    MutableLiveData<MapLordApi.MarkerDeletionResult> data = new MutableLiveData<>();

    // Send the deletion request to the API.
    MapLordApi.MarkerDeletionRequest deletionRequest = new MapLordApi.MarkerDeletionRequest();
    deletionRequest.id = markerInfo.id;
    Call<MapLordApi.MarkerDeletionResult> call = api.deleteMarker(deletionRequest);
    resolveCall(call, (call1, response, err) -> {
      if (err != null) {
        dialogService.fatalError("Error while sending delete marker request: " + err);
        return;
      }

      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          dialogService.fatalError("Error while receiving delete marker response: " + errBody);
        }
        return;
      }

      MapLordApi.MarkerDeletionResult result = response.body();
      assert result != null;
      data.setValue(result);
    });

    return data;
  }

  private static <T> void resolveCall(Call<T> call, CallFinished<T> onCallFinished) {
    call.enqueue(new Callback<>() {
      @Override
      public void onResponse(Call<T> call, Response<T> response) {
        onCallFinished.apply(call, response, null);
      }

      @Override
      public void onFailure(Call<T> call, Throwable t) {
        onCallFinished.apply(call, null, t);
      }
    });
  }

  private interface CallFinished<T> {
    void apply(Call<T> call, Response<T> response, Throwable t);
  }

  private static void dangerousDisableSslCertificateAuthorityValidation(OkHttpClient.Builder builder) {
    // TODO: Remove this when we have a real certificate.
    // TODO: Document this and explain how and why it's dangerous.
    var trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      }
    };

    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("SSL");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    try {
      sslContext.init(null, trustAllCerts, new SecureRandom());
    } catch (KeyManagementException e) {
      throw new RuntimeException(e);
    }

    var socketFactory = sslContext.getSocketFactory();
    builder
      .sslSocketFactory(socketFactory, (X509TrustManager) trustAllCerts[0])
      .hostnameVerifier((hostname, session) -> true);
  }
}
