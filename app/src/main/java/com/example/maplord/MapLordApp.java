package com.example.maplord;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplord.services.ApiService;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.UserService;

public class MapLordApp extends Application {
  private Activity currentActivity;

  // Services
  private DialogService dialogService;
  private ApiService apiService;
  private LocationService locationService;
  private UserService userService;

  @Override
  public void onCreate() {
    super.onCreate();

    initActivityTracking();

    dialogService = new DialogService(this);

    userService = new UserService();

    locationService = new LocationService(this);
  }

  public void initApiService(String authToken, String groupId) {
    apiService = new ApiService(
      getString(R.string.maplord_api_url),
      authToken,
      groupId
    );
    apiService.onError(err -> {
      currentActivity.runOnUiThread(() -> {
        String errorMessage;
        String reportedMessage = err.getMessage();
        if (reportedMessage == null) {
          errorMessage = "Error in connection with server. No error message provided.";
        } else {
          errorMessage = "Error in connection with server: " + reportedMessage;
        }
        dialogService.alert("API error", errorMessage, () -> {
          // TODO: Do nothing here?
        });
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
