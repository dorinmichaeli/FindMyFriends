package com.example.maplord;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplord.api.MapLordApi;
import com.example.maplord.services.MapLordApiService;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapLordApp extends Application {
  private Activity currentActivity;

  // Services
  private DialogService dialogService;
  private MapLordApiService apiService;
  private LocationService locationService;
  private UserService userService;

  @Override
  public void onCreate() {
    super.onCreate();

    initActivityTracking();

    dialogService = new DialogService(this);

    MapLordApi api = createMapLordApi();
    apiService = new MapLordApiService(api, dialogService);

    locationService = new LocationService(this);

    userService = new UserService();
  }

  public Activity getCurrentActivity() {
    assert currentActivity != null;
    return currentActivity;
  }

  public DialogService getDialogService() {
    return dialogService;
  }

  public MapLordApiService getApiService() {
    return apiService;
  }

  public LocationService getLocationService() {
    return locationService;
  }

  public UserService getUserService() {
    return userService;
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
    Activity activity = fragment.requireActivity();
    return get(activity);
  }
}
