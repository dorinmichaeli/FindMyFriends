package com.example.maplord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.databinding.FragmentLoginBinding;
import com.example.maplord.services.DialogService;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
  private FragmentLoginBinding binding;
  private FirebaseAuth firebaseAuth;
  private FirebaseUser user;

  // Dependencies.
  private DialogService dialogService;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    dialogService = MapLordApp.get(this).getDialogService();

    firebaseAuth = FirebaseAuth.getInstance();

    binding = FragmentLoginBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonLogin.setOnClickListener(v -> {
      attemptLogin();
    });
  }

  private void attemptLogin() {
    // Turn off the button while we're attempting to perform the login.
    binding.buttonLogin.setEnabled(false);

    if (user != null) {
      // Huh? The button shouldn't be clickable if we're already logged in,
      // but just we do this check just in case.
      dialogService.alert("Logged in", "You are already logged in.", () -> {
        goToAppAfterLogin();
      });
      return;
    }

    // TODO: The password should probably not be stored in a String, but rather
    //  in some mutable container which we can immediately clear after using.
    // Get the email and password from the text fields.
    String email = binding.textInputEmail.getText().toString();
    String password = binding.textInputPassword.getText().toString();

    if (email.isEmpty() || password.isEmpty()) {
      // The Firebase API doesn't like it when the email or password are
      // empty, and since we know for sure these values are invalid anyway,
      // we just fail the login right here.
      loginFailed(new Exception("Email and password fields cannot be empty."));
      return;
    }

    tryLogin(email, password);
  }

  private void tryLogin(String email, String password) {
    var activity = getActivity();
    assert activity != null;
    firebaseAuth
      .signInWithEmailAndPassword(email, password)
      .addOnSuccessListener(result -> {
        loginSuccess(result);
      })
      .addOnFailureListener(activity, error -> {
        loginFailed(error);
      });
  }

  private void loginSuccess(AuthResult result) {
    user = result.getUser();
    assert user != null;
    String message = String.format("Welcome %s!\nYour user ID is %s.", user.getEmail(), user.getUid());
    dialogService.alert("Login success", message, () -> {
      goToAppAfterLogin();
    });
    // Note: We don't turn the login button back on here because the user is
    // already logged in and if somehow they can still click it we don't want
    // them to be able to do it anyway.
  }

  private void loginFailed(Exception error) {
    dialogService.alert("Login failed", "Unknown password and email combination. ERROR: " + error, () -> {
      // Turn the login button back on to let the user try again.
      binding.buttonLogin.setEnabled(true);
    });
  }

  private void goToAppAfterLogin() {
    NavHostFragment.findNavController(LoginFragment.this)
      .navigate(R.id.action_LoginFragment_to_FirstFragment);
  }
}
