package com.example.maplord.services;

import androidx.appcompat.app.AlertDialog;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;

public class ErrorDialog {
  private final MapLordApp app;

  public ErrorDialog(MapLordApp app) {
    this.app = app;
  }

  public void fatalError(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(app.getCurrentActivity());
    builder
      .setTitle(R.string.fatal_error_exit_title)
      .setMessage(message)
      .setCancelable(false)
      .setPositiveButton("OK", (dialog, id) -> {
        app.getCurrentActivity().finish();
      });
    AlertDialog alert = builder.create();
    alert.show();
  }
}
