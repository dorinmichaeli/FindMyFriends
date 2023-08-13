package com.example.findmyfriends;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.findmyfriends.services.ApiService;
import com.example.findmyfriends.services.DialogService;
import com.example.findmyfriends.services.LocationService;
import com.example.findmyfriends.services.RestApiService;
import com.example.findmyfriends.services.UserService;

public class App extends Application {
  private static final String Tag = "App";

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

    restApiService = new RestApiService(getString(R.string.findmyfriends_rest_api_url), userService);
  }

  private int connectionErrorCount = 0;

  public void initApiService(String authToken, String groupId) {
    apiService = new ApiService(
      getString(R.string.findmyfriends_api_url),
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
        if (connectionErrorCount % 5 == 0) {
          // Show a snackbar every 5th error.
          String errorMessage = "Connection error, trying to reconnect...";
          dialogService.snackbar(errorMessage);
        }
        connectionErrorCount++;
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

  public static App get(Activity activity) {
    return (App) activity.getApplication();
  }

  public static App get(Fragment fragment) {
    Activity activity = fragment.requireActivity();
    return get(activity);
  }
}
