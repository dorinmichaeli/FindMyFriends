package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.UserService;
import com.google.firebase.auth.FirebaseUser;

public class PrepareActivity extends AppCompatActivity {
  private boolean locationUpdated = false;
  private boolean authTokenReceived = false;
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

    dialogService = MapLordApp.get(this).getDialogService();
    locationService = MapLordApp.get(this).getLocationService();
    userService = MapLordApp.get(this).getUserService();

    FirebaseUser user = getIntent().getParcelableExtra("user");
    userService.setUser(user);

    // TODO: This should be done in a better way.
    userService.getAuthToken().addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        runOnUiThread(() -> {
          dialogService.fatalError("Failed to get auth token: " + task.getException().getMessage());
        });
        return;
      }
      MapLordApp.get(this).initApiService(task.getResult(), "xyz");
      // TODO: Should probably listen to welcome event and store chat messages and markers here.
      authTokenReceived = true;
      checkIfEverythingFinished();
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
    if (locationUpdated && authTokenReceived) {
      var intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }
  }
}
