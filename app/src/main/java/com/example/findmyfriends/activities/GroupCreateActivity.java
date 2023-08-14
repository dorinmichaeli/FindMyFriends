package com.example.findmyfriends.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.example.findmyfriends.services.DialogService;
import com.example.findmyfriends.services.RestApiService;

public class GroupCreateActivity extends AppCompatActivity {
  // Dependencies.
  private DialogService dialogService;
  private RestApiService restApiService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_create);

    dialogService = App.get(this).getDialogService();
    restApiService = App.get(this).getRestApiService();

    EditText editTextGroupName = findViewById(R.id.edit_text_group_name);
    EditText editTextEventId = findViewById(R.id.edit_text_event_id);
    Button buttonSendGroupId = findViewById(R.id.button_finish_creating_group);

    buttonSendGroupId.setOnClickListener(view -> {
      // Take the group id from the input field.
      String groupName = editTextGroupName.getText().toString();
      String eventId = editTextEventId.getText().toString();

      restApiService.createNewGroup(groupName, eventId, (newGroup, err) -> {
        if (err != null) {
          dialogService.alert("Failed to create group", "Something went wrong, please try again.", null);
          return;
        }

        // Copy the id of the newly created group to the clipboard.
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("groupId", newGroup.groupId);
        clipboard.setPrimaryClip(clip);

        String message = String.format(
          "Group \"%s\" created successfully. Your group id is: %s. It has been copied to your clipboard!",
          newGroup.groupName, newGroup.groupId);
        dialogService.alert("Group created successfully!", message, () -> {
          finishCreatingGroup();
        });
      });
    });
  }

  private void finishCreatingGroup() {
    var intent = new Intent(GroupCreateActivity.this, TitleMenuActivity.class);
    startActivity(intent);
  }
}
