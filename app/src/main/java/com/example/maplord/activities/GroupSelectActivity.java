package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.maplord.R;

public class GroupSelectActivity extends AppCompatActivity {
  private EditText editTextGroupId;
  private Button buttonSendGroupId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_select);

    editTextGroupId = findViewById(R.id.editTextGroupId);
    buttonSendGroupId = findViewById(R.id.buttonSendGroupId);

    buttonSendGroupId.setOnClickListener(view -> {
      // Take the group id from the input field.
      String groupId = editTextGroupId.getText().toString();

      // TODO: Validation for groupId can be added here

      // Start the next activity, and pass the group id to it.
      var intent = new Intent(GroupSelectActivity.this, PrepareActivity.class);
      intent.putExtra("groupId", groupId);
      startActivity(intent);
    });
  }
}
