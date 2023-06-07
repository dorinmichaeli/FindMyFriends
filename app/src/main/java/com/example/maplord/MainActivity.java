package com.example.maplord;

import android.os.Bundle;

import com.example.maplord.services.LocationService;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.maplord.databinding.ActivityMainBinding;
import com.example.maplord.services.UserService;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
  private AppBarConfiguration appBarConfiguration;

  // Dependencies.
  private LocationService locationService;
  private UserService userService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    locationService = MapLordApp.get(this).getLocationService();
    userService = MapLordApp.get(this).getUserService();

    FirebaseUser user = getIntent().getParcelableExtra("user");
    userService.setUser(user);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setSupportActionBar(binding.toolbar);

    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    requestAppPermissions();
  }

  private void requestAppPermissions() {
    locationService.requestLocationPermissions(this);
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, appBarConfiguration)
      || super.onSupportNavigateUp();
  }
}
