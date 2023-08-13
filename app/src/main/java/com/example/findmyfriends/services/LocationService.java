package com.example.findmyfriends.services;

import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Map;
import java.util.function.Consumer;

public class LocationService {
  private final App app;
  private final FusedLocationProviderClient fusedLocationClient;
  private Location lastKnownLocation = null;

  public LocationService(App app) {
    this.app = app;
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(app);
  }

  public LiveData<Boolean> updateLastKnownLocation() {
    var data = new MutableLiveData<>(false);

    if (ActivityCompat.checkSelfPermission(app, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
      && ActivityCompat.checkSelfPermission(app, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      showFatalDialogLocationPermissionMissing();
    }

    fusedLocationClient
      .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
      .addOnSuccessListener(app.getCurrentActivity(), location -> {
        assert location != null;
        lastKnownLocation = location;
        data.setValue(true);
      })
      .addOnFailureListener(app.getCurrentActivity(), error -> {
        // TODO: Handle failure.
        app.getDialogService()
          .fatalError("Failed to get device location: " + error);
      });

    return data;
  }

  public Location getLastKnownLocation() {
    assert lastKnownLocation != null;
    return lastKnownLocation;
  }

  public void showFatalDialogLocationPermissionMissing() {
    // Our application cannot work without fine location access, and so if
    // the user does not grant it there's nothing else we can do other than
    // let the user know and exit the application.
    // Hopefully the user will restart the application and grant the
    // permission.

    // See: https://developer.android.com/develop/ui/views/components/dialogs#AlertDialog
    app.getDialogService()
      .fatalError(app.getString(R.string.location_permission_denied_exit_message));
  }

  public void requestLocationAndBluetoothPermissions(AppCompatActivity activity, Consumer<Boolean> callback) {
    requestPermissions(
      activity,
      new String[]{
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_ADVERTISE,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
      },
      result -> {
        // Permission request process has completed.

        boolean allGranted = true;
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
          if (!entry.getValue()) {
            allGranted = false;
            break;
          }
        }

        callback.accept(allGranted);
      }
    );
  }

  private void requestPermissions(
    AppCompatActivity activity,
    String[] permissions,
    ActivityResultCallback<Map<String, Boolean>> callback
  ) {
    ActivityResultLauncher<String[]> permissionRequest =
      activity.registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(),
        callback
      );
    permissionRequest.launch(permissions);
  }
}
