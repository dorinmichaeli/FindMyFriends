package com.example.maplord;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

public class ErrorDialog {
  public static void fatalError(Activity activity, String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder
      .setTitle(R.string.fatal_error_exit_title)
      .setMessage(message)
      .setCancelable(false)
      .setPositiveButton("OK", (dialog, id) -> {
        // TODO: Is this better than finish()? -> activity.finishAndRemoveTask()
        activity.finish();
      });
    AlertDialog alert = builder.create();
    alert.show();
  }
}
