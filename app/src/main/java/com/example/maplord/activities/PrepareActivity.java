package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.ApiService;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.UserService;

public class PrepareActivity extends AppCompatActivity {
  private boolean locationUpdated = false;
  private boolean welcomeMessageReceived = false;
  // TODO: Load marker list here, probably.
  //private boolean markerListLoaded = false;

  // Dependencies.
  private DialogService dialogService;
  private LocationService locationService;
  private UserService userService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prepare);

    String groupId = getIntent().getStringExtra("groupId");

    dialogService = MapLordApp.get(this).getDialogService();
    locationService = MapLordApp.get(this).getLocationService();
    userService = MapLordApp.get(this).getUserService();

    // TODO: This should be done in a better way.
    userService.getAuthToken().addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        runOnUiThread(() -> {
          dialogService.fatalError("Failed to get auth token: " + task.getException().getMessage());
        });
        return;
      }

      MapLordApp.get(this).initApiService(task.getResult(), groupId);
      ApiService apiService = MapLordApp.get(this).getApiService();
      apiService.onWelcomeMessage(message -> {
        welcomeMessageReceived = true;
        checkIfEverythingFinished();
      });
    });

    startLoadingLastKnownLocation();
  }

  private void startLoadingLastKnownLocation() {
    LiveData<Boolean> locationUpdated = locationService.updateLastKnownLocation();
    locationUpdated.observe(this, updated -> {
      if (!updated) {
        return;
      }
      this.locationUpdated = true;
      checkIfEverythingFinished();
    });
  }

  private void checkIfEverythingFinished() {
    if (locationUpdated && welcomeMessageReceived) {
      var intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }
  }
}
