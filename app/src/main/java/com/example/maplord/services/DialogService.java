package com.example.maplord.services;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.tools.Action;
import com.google.android.material.snackbar.Snackbar;

import java.util.function.Consumer;
import java.util.function.Function;

public class DialogService {
  private final MapLordApp app;

  public DialogService(MapLordApp app) {
    this.app = app;
  }

  public void fatalError(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(app.getCurrentActivity());
    builder
      .setTitle(R.string.fatal_error_exit_title)
      .setMessage(message)
      .setCancelable(false)
      .setPositiveButton("OK", (dialog, id) -> {
        app.getCurrentActivity().finishAffinity();
      });
    AlertDialog alert = builder.create();
    alert.show();
  }


  public void alert(String title, String message, @Nullable Action onClose) {
    AlertDialog.Builder builder = new AlertDialog.Builder(app.getCurrentActivity());
    builder
      .setTitle(title)
      .setMessage(message)
      .setCancelable(false)
      .setPositiveButton("OK", (dialog, id) -> {
        dialog.cancel();
        if (onClose != null) {
          onClose.apply();
        }
      });
    AlertDialog alert = builder.create();
    alert.show();
  }

  public void snackbar(String message) {
    View view = app.getCurrentActivity().findViewById(android.R.id.content);
    var snackbar = Snackbar.make(
      view,
      message,
      Snackbar.LENGTH_INDEFINITE);
    snackbar.setAction("CLOSE", v -> {
      snackbar.dismiss();
    });
    snackbar.show();
  }

  public void textInputPopup(String title, Consumer<String> result) {
    var activity = app.getCurrentActivity();

    var input = new EditText(activity);
    input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

    var dialog = new AlertDialog.Builder(activity)
      .setTitle(title)
      .setView(input)
      .setPositiveButton("OK", (d, whichButton) -> {
        String value = input.getText().toString();
        result.accept(value);
      })
      .create();

    dialog.show();
  }
}
