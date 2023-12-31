package com.example.findmyfriends.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.example.findmyfriends.services.ApiService;
import com.example.findmyfriends.services.DialogService;
import com.example.findmyfriends.services.UserService;

public class PrepareActivity extends AppCompatActivity {
  private boolean welcomeMessageReceived = false;

  // Dependencies.
  private DialogService dialogService;
  private UserService userService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prepare);

    String groupId = getIntent().getStringExtra("groupId");

    dialogService = App.get(this).getDialogService();
    userService = App.get(this).getUserService();

    userService.getAuthToken().addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        runOnUiThread(() -> {
          dialogService.fatalError("Failed to get auth token: " + task.getException().getMessage());
        });
        return;
      }

      App.get(this).initApiService(task.getResult(), groupId);
      ApiService apiService = App.get(this).getApiService();

      apiService.onConnectedToServer(() -> {
        welcomeMessageReceived = true;
        checkIfEverythingFinished();
      });
    });

    ProgressBar pb = findViewById(R.id.progress_bar);
    pb.animate();
  }

  private void checkIfEverythingFinished() {
    if (welcomeMessageReceived) {
      var intent = new Intent(this, MainActivity.class);
      startActivity(intent);
    }
  }
}
