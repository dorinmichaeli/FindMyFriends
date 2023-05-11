package com.example.maplord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplord.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
  private FragmentLoginBinding binding;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentLoginBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonLogin.setOnClickListener(v -> {
      String email = binding.textInputEmail.getText().toString();
      // TODO: The password should probably not be stored in a String.
      String password = binding.textInputPassword.getText().toString();

      // TODO: Implement login with Firebase!
      System.out.println(email);
    });
  }
}
