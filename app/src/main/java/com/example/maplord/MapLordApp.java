package com.example.maplord;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplord.api.MapLordApi;
import com.example.maplord.api.MapLordModel;
import com.example.maplord.services.ErrorDialog;
import com.example.maplord.services.LocationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapLordApp extends Application {
  private Activity currentActivity;

  // Services
  private ErrorDialog errorDialog;
  private MapLordModel apiModel;
  private LocationService locationService;

  @Override
  public void onCreate() {
    super.onCreate();

    initActivityTracking();

    errorDialog = new ErrorDialog(this);

    MapLordApi api = createMapLordApi();
    apiModel = new MapLordModel(api, errorDialog);

    locationService = new LocationService(this);
  }

  public Activity getCurrentActivity() {
    assert currentActivity != null;
    return currentActivity;
  }

  public ErrorDialog getErrorDialog() {
    return errorDialog;
  }

  public MapLordModel getApiModel() {
    return apiModel;
  }

  public LocationService getLocationService() {
    return locationService;
  }

  private void initActivityTracking() {
    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        currentActivity = activity;
      }

      @Override
      public void onActivityStarted(@NonNull Activity activity) {
      }

      @Override
      public void onActivityResumed(@NonNull Activity activity) {
      }

      @Override
      public void onActivityPaused(@NonNull Activity activity) {
      }

      @Override
      public void onActivityStopped(@NonNull Activity activity) {
      }

      @Override
      public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
      }

      @Override
      public void onActivityDestroyed(@NonNull Activity activity) {
      }
    });
  }

  private static MapLordApi createMapLordApi() {
    Gson gson = new GsonBuilder()
      .create();

    OkHttpClient client = new OkHttpClient.Builder()
      .build();

    Retrofit retrofit = new Retrofit.Builder()
      // TODO: Export the URL to a resource file or whatever?
      .baseUrl("http://maplord.api:3000")
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build();

    MapLordApi api = retrofit.create(MapLordApi.class);

    return api;
  }

  public static MapLordApp get(Activity activity) {
    return (MapLordApp) activity.getApplication();
  }

  public static MapLordApp get(Fragment fragment) {
    Activity activity = fragment.getActivity();
    assert activity != null;
    return get(activity);
  }
}
