package com.example.maplord.activities;

import android.os.Bundle;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.adapter.TabsAdapter;
import com.example.maplord.services.LocationService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.maplord.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
  // Dependencies.
  private LocationService locationService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    locationService = MapLordApp.get(this).getLocationService();

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    initTabs();

    // TODO: Should this be done in the very first activity?
    requestAppPermissions();
  }

  private void initTabs() {
    ViewPager2 viewPager = findViewById(R.id.view_pager);
    viewPager.setUserInputEnabled(false);
    TabLayout tabs = findViewById(R.id.tabs);

    var tabsAdapter = new TabsAdapter(this);
    viewPager.setAdapter(tabsAdapter);

    new TabLayoutMediator(tabs, viewPager,
      (tab, position) -> {
        switch (position) {
          case 0:
            tab.setText("Group");
            break;
          case 1:
            tab.setText("Chat");
            break;
          case 2:
            tab.setText("Map");
            break;
        }
      }).attach();
  }

  private void requestAppPermissions() {
    locationService.requestLocationPermissions(this);
  }
}
