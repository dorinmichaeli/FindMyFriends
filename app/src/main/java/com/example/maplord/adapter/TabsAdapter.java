package com.example.maplord.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.maplord.fragments.MapboxFragment;
import com.example.maplord.fragments.GroupFragment;
import com.example.maplord.fragments.ChatFragment;

public class TabsAdapter extends FragmentStateAdapter {
  private final MapboxFragment mapboxFragment;
  private final ChatFragment chatFragment;
  private final GroupFragment groupFragment;

  public TabsAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);

    mapboxFragment = new MapboxFragment();
    chatFragment = new ChatFragment();
    groupFragment = new GroupFragment();
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    switch (position) {
      case 0:
        return mapboxFragment;
      case 1:
        return chatFragment;
      case 2:
        return groupFragment;
      default:
        throw new IllegalArgumentException("Invalid position");
    }
  }

  @Override
  public int getItemCount() {
    return 3;
  }
}
