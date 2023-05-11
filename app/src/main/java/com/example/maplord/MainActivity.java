package com.example.maplord;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.example.maplord.api.MapLordApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.maplord.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
  private AppBarConfiguration appBarConfiguration;
  private ActivityMainBinding binding;
  private FusedLocationProviderClient fusedLocationClient;
  private Location lastKnownLocation = null;
  private List<MapLordApi.MarkerInfo> preExistingMarkers = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setSupportActionBar(binding.toolbar);

    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
      .setAction("Action", null).show());

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    requestPermissions(
      new String[]{
        android.Manifest.permission.ACCESS_FINE_LOCATION,
      },
      result -> {
        Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
        if (fineLocationGranted == null || !fineLocationGranted) {
          onLocationPermissionRefused();
        }
      }
    );
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, appBarConfiguration)
      || super.onSupportNavigateUp();
  }

  public LiveData<Boolean> updateLastKnownLocation() {
    var data = new MutableLiveData<>(false);

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
      && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      onLocationPermissionRefused();
    }

    fusedLocationClient
      .getLastLocation()
      .addOnSuccessListener(this, location -> {
        lastKnownLocation = location;
        data.setValue(true);
      })
      .addOnFailureListener(this, error -> {
        // TODO: Handle failure.
        MapLordApp.get(this)
          .getErrorDialog()
          .fatalError("Failed to get device location: " + error);
      });

    return data;
  }

  public LiveData<Boolean> updatePreExistingMarkers() {
    var data = new MutableLiveData<>(false);

    MapLordApp.get(this)
      .getApiModel()
      .apiListMarkers().observe(this, markerList -> {
        preExistingMarkers = markerList;
        data.setValue(true);
      });

    return data;
  }

  public List<MapLordApi.MarkerInfo> getPreExistingMarkers() {
    assert preExistingMarkers != null;
    return preExistingMarkers;
  }

  public Location getLastKnownLocation() {
    // TODO: What happens if the caller gets an exception?
    assert lastKnownLocation != null;
    return lastKnownLocation;
  }

  private void onLocationPermissionRefused() {
    // Our application cannot work without fine location access, and so if
    // the user does not grant it there's nothing else we can do other than
    // let the user know and exit the application.
    // Hopefully the user will restart the application and grant the
    // permission.

    // See: https://developer.android.com/develop/ui/views/components/dialogs#AlertDialog
    MapLordApp.get(this).getErrorDialog()
      .fatalError(getString(R.string.location_permission_denied_exit_message));
  }

  private void exitApplication() {
    // Finishes all activities in this task and
    // removes it from the recent tasks list.
    finishAndRemoveTask();
  }

  private void requestPermissions(String[] permissions, ActivityResultCallback<Map<String, Boolean>> callback) {
    ActivityResultLauncher<String[]> locationPermissionRequest =
      registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(),
        callback
      );
    locationPermissionRequest.launch(permissions);
  }
}