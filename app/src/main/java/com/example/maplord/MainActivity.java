package com.example.maplord;

import android.os.Bundle;

import com.example.maplord.api.MapLordApi;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.maplord.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {
  private AppBarConfiguration appBarConfiguration;
  private ActivityMainBinding binding;
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

    MapLordApp.get(this)
      .getLocationService()
      .requestLocationPermissions(this);
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, appBarConfiguration)
      || super.onSupportNavigateUp();
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

  private void exitApplication() {
    // Finishes all activities in this task and
    // removes it from the recent tasks list.
    finishAndRemoveTask();
  }
}