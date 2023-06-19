package com.example.maplord.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.RestApiService;

public class GroupSelectActivity extends AppCompatActivity {
  // Dependencies.
  private DialogService dialogService;
  private RestApiService restApiService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_select);

    dialogService = MapLordApp.get(this).getDialogService();
    restApiService = MapLordApp.get(this).getRestApiService();

    EditText editTextGroupId = findViewById(R.id.editTextGroupId);
    Button buttonSendGroupId = findViewById(R.id.buttonSendGroupId);

    buttonSendGroupId.setOnClickListener(view -> {
      // Take the group id from the input field.
      String groupId = editTextGroupId.getText().toString();

      restApiService.checkGroupExists(groupId, (exists, err) -> {
        if (err != null) {
          dialogService.alert("Something went wrong", "Please try again.", null);
          return;
        }
        if (!exists) {
          dialogService.alert("No such group", "No group with this id exists. Try again.", null);
          return;
        }
        finishSelectingGroup(groupId);
      });
    });
  }

  private void finishSelectingGroup(String groupId) {
    // Start the next activity, and pass the group id to it.
    var intent = new Intent(GroupSelectActivity.this, PrepareActivity.class);
    intent.putExtra("groupId", groupId);
    startActivity(intent);
  }
}
