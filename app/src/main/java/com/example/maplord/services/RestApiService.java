package com.example.maplord.services;

import com.example.maplord.api.EventCreateRequest;
import com.example.maplord.api.EventCreateResponse;
import com.example.maplord.api.GroupCreateRequest;
import com.example.maplord.api.GroupCreateResponse;
import com.example.maplord.api.MarkerLocation;
import com.example.maplord.api.RestApi;
import com.example.maplord.tools.Helpers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.function.BiConsumer;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestApiService {
  private final RestApi api;

  public RestApiService(String apiUrl, UserService userService) {
    OkHttpClient client = new OkHttpClient.Builder()
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

    Gson gson = new GsonBuilder()
      .create();

    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(apiUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build();

    api = retrofit.create(RestApi.class);
  }

  public void checkGroupExists(String groupId, BiConsumer<Boolean, Throwable> onResult) {
    var call = api.doesGroupExist(groupId);
    Helpers.resolveCall(call, (result, err) -> {
      if (err != null) {
        onResult.accept(null, err);
        return;
      }

      onResult.accept(result.exists, null);
    });
  }

  public void createNewGroup(String groupName, String eventId, BiConsumer<GroupCreateResponse, Throwable> onResult) {
    var request = new GroupCreateRequest();
    request.groupName = groupName;
    request.eventId = eventId;
    var call = api.createGroup(request);
    Helpers.resolveCall(call, (result, err) -> {
      if (err != null) {
        onResult.accept(null, err);
        return;
      }
      onResult.accept(result, null);
    });
  }

  public void createNewEvent(String eventName, MarkerLocation[] markerLocations, BiConsumer<EventCreateResponse, Throwable> onResult) {
    var request = new EventCreateRequest();
    request.eventName = eventName;
    request.markerLocations = markerLocations;

    var call = api.createEvent(request);
    Helpers.resolveCall(call, (result, err) -> {
      if (err != null) {
        onResult.accept(null, err);
        return;
      }
      onResult.accept(result, null);
    });
  }
}
