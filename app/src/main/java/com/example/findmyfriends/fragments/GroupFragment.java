package com.example.findmyfriends.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.example.findmyfriends.databinding.FragmentGroupBinding;
import com.example.findmyfriends.services.ApiService;

import java.util.ArrayList;

public class GroupFragment extends Fragment {
  private FragmentGroupBinding binding;
  private ListView listView;
  private ArrayList<String> userList;
  private ArrayAdapter<String> listAdapter;

  // Dependencies.
  private ApiService apiService;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    apiService = App.get(this).getApiService();

    binding = FragmentGroupBinding.inflate(inflater, container, false);

    // Get the list view.
    listView = binding.listView;
    // Create the underlying data source.
    userList = new ArrayList<>();
    // Create the collection adapter.
    listAdapter = new ArrayAdapter<>(requireContext(), R.layout.group_user_item, userList);
    // Set the adapter.
    listView.setAdapter(listAdapter);

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    apiService.onUserJoined((userJoined, isNew) -> {
      addItemToListView(userJoined.userName);
    });
    apiService.onUserLeft(userLeft -> {
      removeItemFromListView(userLeft.userName);
    });

  }

  private void addItemToListView(String item) {
    requireActivity().runOnUiThread(() -> {
      if (userList.contains(item)) {
        // Don't add the item if it's already in the list.
        return;
      }
      // Add the item to the list.
      userList.add(item);
      // Notify the adapter that the data source has changed.
      listAdapter.notifyDataSetChanged();
    });
  }

  private void removeItemFromListView(String item) {
    requireActivity().runOnUiThread(() -> {
      // Remove the item from the list.
      boolean removed = userList.remove(item);
      if (removed) {
        // Notify the adapter that the data source has changed.
        listAdapter.notifyDataSetChanged();
      }
    });
  }
}
