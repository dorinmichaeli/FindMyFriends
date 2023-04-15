package com.example.maplord;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.maplord.databinding.ActivityMainBinding;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

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
                if (fineLocationGranted != null && fineLocationGranted) {
                    onLocationPermissionGranted();
                } else {
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

    @SuppressLint("MissingPermission")
    private void onLocationPermissionGranted() {
        fusedLocationClient
            .getLastLocation()
            .addOnSuccessListener(this, location -> {
                // TODO: Do something with the location.
                System.out.println("Location: " + location);
            });
    }

    private void onLocationPermissionRefused() {
        // Our application cannot work without fine location access, and so if
        // the user does not grant it there's nothing else we can do other than
        // let the user know and exit the application.
        // Hopefully the user will restart the application and grant the
        // permission.

        // See: https://developer.android.com/develop/ui/views/components/dialogs#AlertDialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setMessage(R.string.location_permission_denied_exit_message)
            .setPositiveButton("OK", (dialog, id) -> exitApplication());

        AlertDialog alert = builder.create();
        alert.setTitle(R.string.location_permission_denied_exit_title);
        alert.show();
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