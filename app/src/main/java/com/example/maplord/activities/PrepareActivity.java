package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.ApiService;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.UserService;

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

    dialogService = MapLordApp.get(this).getDialogService();
    userService = MapLordApp.get(this).getUserService();

    // TODO: This should be done in a better way.
    userService.getAuthToken().addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        runOnUiThread(() -> {
          dialogService.fatalError("Failed to get auth token: " + task.getException().getMessage());
        });
        return;
      }

      MapLordApp.get(this).initApiService(task.getResult(), groupId);
      ApiService apiService = MapLordApp.get(this).getApiService();
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
