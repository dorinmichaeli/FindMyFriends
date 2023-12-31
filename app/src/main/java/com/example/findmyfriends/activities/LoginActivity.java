package com.example.findmyfriends.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.example.findmyfriends.services.DialogService;
import com.example.findmyfriends.services.UserService;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public final class LoginActivity extends AppCompatActivity {
  private FirebaseAuth firebaseAuth;
  private SignInClient loginClient;
  private ActivityResultLauncher<IntentSenderRequest> resultLauncher;

  // Services.
  private DialogService dialogService;
  private UserService userService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Get services.
    dialogService = App.get(this).getDialogService();
    userService = App.get(this).getUserService();

    FirebaseAuth.getInstance().addAuthStateListener(auth -> {
      firebaseAuth = auth;

      // We'll use this client to prompt the user to log in with their google account.
      loginClient = Identity.getSignInClient(this);

      FirebaseUser currentUser = firebaseAuth.getCurrentUser();
      if (currentUser == null) {
        return;
      }
      userService.setUser(currentUser);
      finishLogin();
    });

    resultLauncher = registerForActivityResult(
      new ActivityResultContracts.StartIntentSenderForResult(),
      result -> tryLogin(result.getData())
    );

    var button = (Button) findViewById(R.id.login_button);
    assert button != null;
    button.setOnClickListener((View view) -> {
      beginSignInRequest();
    });
  }

  private void beginSignInRequest() {
    var tokenOptions = BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
      .setSupported(true)
      // Your server's client ID, not your Android client ID.
      .setServerClientId(getString(R.string.default_web_client_id))
      // false -> Also show accounts that haven't registered yet.
      .setFilterByAuthorizedAccounts(false)
      .build();

    var signInRequest = BeginSignInRequest.builder()
      .setGoogleIdTokenRequestOptions(tokenOptions)
      .setAutoSelectEnabled(false)
      .build();

    loginClient.beginSignIn(signInRequest)
      .addOnSuccessListener(this, result -> {
        IntentSender sender = result.getPendingIntent().getIntentSender();
        var request = new IntentSenderRequest.Builder(sender).build();
        resultLauncher.launch(request);
      })
      .addOnFailureListener(this, err -> {
        // TODO: No saved credentials found. Launch the One Tap sign-up flow, or
        //  do nothing and continue presenting the signed-out UI.
        dialogService.fatalError("Could not log in: " + err);
      });
  }

  private void tryLogin(Intent data) {
    SignInCredential googleCredential;
    try {
      googleCredential = loginClient.getSignInCredentialFromIntent(data);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }

    // Got an ID token from Google.
    // Use it to authenticate with Firebase.
    String idToken = googleCredential.getGoogleIdToken();
    assert idToken != null;

    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
    firebaseAuth.signInWithCredential(firebaseCredential)
      .addOnFailureListener(this, err -> {
        dialogService.fatalError("Could not sign in with credentials: " + err);
      });
  }

  private void finishLogin() {
    var intent = new Intent(this, TitleMenuActivity.class);
    startActivity(intent);
  }
}
