package com.example.maplord;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplord.services.ApiService;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.RestApiService;
import com.example.maplord.services.UserService;

public class MapLordApp extends Application {
  private static final String Tag = "MapLordApp";

  private Activity currentActivity;

  // Services
  private DialogService dialogService;
  private ApiService apiService;
  private RestApiService restApiService;
  private LocationService locationService;
  private UserService userService;

  @Override
  public void onCreate() {
    super.onCreate();

    initActivityTracking();

    dialogService = new DialogService(this);

    userService = new UserService();

    locationService = new LocationService(this);

    restApiService = new RestApiService(getString(R.string.maplord_rest_api_url), userService);
  }

  public void initApiService(String authToken, String groupId) {
    apiService = new ApiService(
      getString(R.string.maplord_api_url),
      authToken,
      groupId
    );
    apiService.onConnectedToServer(() -> {
      currentActivity.runOnUiThread(() -> {
        dialogService.snackbar("Connected to server.");
      });
    });
    apiService.onError(err -> {
      Log.d(Tag, "API error: " + err.getMessage());
      currentActivity.runOnUiThread(() -> {
        String errorMessage = "Connection error, trying to reconnect...";
        dialogService.snackbar(errorMessage);
      });
    });
  }

  public Activity getCurrentActivity() {
    assert currentActivity != null;
    return currentActivity;
  }

  public DialogService getDialogService() {
    return dialogService;
  }

  public ApiService getApiService() {
    assert apiService != null;
    return apiService;
  }

  public RestApiService getRestApiService() {
    assert restApiService != null;
    return restApiService;
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

  // Helper methods.

  public static MapLordApp get(Activity activity) {
    return (MapLordApp) activity.getApplication();
  }

  public static MapLordApp get(Fragment fragment) {
    Activity activity = fragment.requireActivity();
    return get(activity);
  }
}
