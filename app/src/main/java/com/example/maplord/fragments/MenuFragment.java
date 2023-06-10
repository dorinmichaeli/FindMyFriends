package com.example.maplord.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.databinding.FragmentMenuBinding;
import com.example.maplord.services.UserService;

public class MenuFragment extends Fragment {
  private FragmentMenuBinding binding;

  // Services.
  private UserService userService;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Get services.
    userService = MapLordApp.get(this).getUserService();
  }

  @Override
  public View onCreateView(
    LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState
  ) {
    binding = FragmentMenuBinding.inflate(inflater, container, false);

    String welcomeMessage = "Welcome, " + userService.getUserEmail();
    binding.textviewMenu.setText(welcomeMessage);

    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonMenu.setOnClickListener(view1 -> {
      NavHostFragment.findNavController(MenuFragment.this)
        .navigate(R.id.action_MenuFragment_to_LoaderFragment);
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

}
