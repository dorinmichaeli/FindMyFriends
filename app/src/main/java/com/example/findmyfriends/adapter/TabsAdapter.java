package com.example.findmyfriends.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.findmyfriends.fragments.BluetoothChatFragment;
import com.example.findmyfriends.fragments.MapboxFragment;
import com.example.findmyfriends.fragments.GroupFragment;
import com.example.findmyfriends.fragments.ChatFragment;

public class TabsAdapter extends FragmentStateAdapter {
  private final MapboxFragment mapboxFragment;
  private final ChatFragment chatFragment;
  private final GroupFragment groupFragment;
  private final BluetoothChatFragment btChatFragment;

  public TabsAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);

    mapboxFragment = new MapboxFragment();
    chatFragment = new ChatFragment();
    groupFragment = new GroupFragment();
    btChatFragment = new BluetoothChatFragment();
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
      case 3:
        return btChatFragment;
      default:
        throw new IllegalArgumentException("Invalid position");
    }
  }

  @Override
  public int getItemCount() {
    return 4;
  }
}
