package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.MapLordApiService;
import com.example.maplord.services.UserService;
import com.google.firebase.auth.FirebaseUser;

public class PrepareActivity extends AppCompatActivity {
  private boolean locationUpdated = false;
  private boolean markerListLoaded = false;

  // Dependencies.
  private MapLordApiService apiService;
  private LocationService locationService;
  private UserService userService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prepare);

    apiService = MapLordApp.get(this).getApiService();
    locationService = MapLordApp.get(this).getLocationService();
    userService = MapLordApp.get(this).getUserService();

    FirebaseUser user = getIntent().getParcelableExtra("user");
    userService.setUser(user);

    startLoadingLastKnownLocation();
    startLoadingPreExistingMarkers();
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

  private void startLoadingPreExistingMarkers() {
    LiveData<Boolean> markersUpdated = apiService.updatePreExistingMarkers();
    markersUpdated.observe(this, updated -> {
      if (!updated) {
        return;
      }
      this.markerListLoaded = true;
      checkIfEverythingFinished();
    });
  }

  private void checkIfEverythingFinished() {
    if (locationUpdated && markerListLoaded) {
      var intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }
  }
}
