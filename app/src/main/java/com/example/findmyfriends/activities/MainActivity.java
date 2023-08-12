package com.example.findmyfriends.activities;

import android.os.Bundle;

import com.example.findmyfriends.R;
import com.example.findmyfriends.adapter.TabsAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.findmyfriends.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    initTabs();
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
            tab.setText("Map");
            break;
          case 1:
            tab.setText("Chat");
            break;
          case 2:
            tab.setText("Group");
            break;
          case 3:
            tab.setText("\uD83D\uDCF6");
            break;
        }
      }).attach();
  }
}
