package com.example.maplord.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.services.DialogService;
import com.example.maplord.services.RestApiService;

public class GroupCreateActivity extends AppCompatActivity {
  // Dependencies.
  private DialogService dialogService;
  private RestApiService restApiService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_create);

    dialogService = MapLordApp.get(this).getDialogService();
    restApiService = MapLordApp.get(this).getRestApiService();

    EditText editTextGroupId = findViewById(R.id.edit_text_group_name);
    Button buttonSendGroupId = findViewById(R.id.button_finish_creating_group);

    buttonSendGroupId.setOnClickListener(view -> {
      // Take the group id from the input field.
      String groupId = editTextGroupId.getText().toString();

      restApiService.createNewGroup(groupId, (newGroup, err) -> {
        if (err != null) {
          dialogService.alert("Something went wrong", "Please try again.", null);
          return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("groupId", newGroup.groupId);
        clipboard.setPrimaryClip(clip);

        String message = String.format(
          "Group \"%s\" created successfully. Your group id is: %s. It has been copied to your clipboard!",
          newGroup.groupName, newGroup.groupId);
        dialogService.alert("Group created", message, () -> {
          finishCreatingGroup(newGroup.groupId);
        });
      });
    });
  }

  private void finishCreatingGroup(String groupId) {
    // Start the next activity, and pass the group id to it.
    var intent = new Intent(GroupCreateActivity.this, TitleMenuActivity.class);
    intent.putExtra("groupId", groupId);
    startActivity(intent);
  }
}
