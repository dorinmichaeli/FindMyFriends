package com.example.maplord.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.maplord.fragments.MapboxFragment;
import com.example.maplord.fragments.GroupFragment;
import com.example.maplord.fragments.ChatFragment;

public class TabsAdapter extends FragmentStateAdapter {

  public TabsAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    switch (position) {
      case 0:
        return new GroupFragment();
      case 1:
        return new ChatFragment();
      case 2:
        return new MapboxFragment();
      default:
        throw new IllegalArgumentException("Invalid position");
    }
  }

  @Override
  public int getItemCount() {
    return 3;
  }
}
