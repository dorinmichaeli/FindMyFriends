package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maplord.R;

public class TitleMenuActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_title_menu);

    findViewById(R.id.button_join_group).setOnClickListener(v -> {
      startGroupSelectActivity();
    });

    findViewById(R.id.button_create_group).setOnClickListener(v -> {
      startGroupCreateActivity();
    });

    findViewById(R.id.button_create_event).setOnClickListener(v -> {
      startEventCreateActivity();
    });

  }

  private void startGroupSelectActivity() {
    var intent = new Intent(this, GroupSelectActivity.class);
    startActivity(intent);
  }

  private void startGroupCreateActivity() {
    // TODO: Implement this.
  }

  private void startEventCreateActivity() {
    // TODO: Implement this.
  }
}
