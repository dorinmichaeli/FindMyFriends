package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.LocationService;

public class PermissionsActivity extends AppCompatActivity {
  // Dependencies.
  private LocationService locationService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_permissions);

    locationService = MapLordApp.get(this).getLocationService();

    locationService.requestLocationPermissions(this, fineLocationPermissionGranted -> {
      if (!fineLocationPermissionGranted) {
        locationService.showFatalDialogLocationPermissionMissing();
        return;
      }

      // TODO: This should probably not be done here.
      LiveData<Boolean> locationUpdated = locationService.updateLastKnownLocation();
      locationUpdated.observe(this, updated -> {
        if (!updated) {
          return;
        }
        continueToLoginActivity();
      });
    });
  }

  private void continueToLoginActivity() {
    var intent = new Intent(this, LoginActivity.class);
    startActivity(intent);
  }
}
