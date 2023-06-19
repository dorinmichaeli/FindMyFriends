package com.example.maplord.tools;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Locale;
import java.util.function.BiConsumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Helpers {
  public static <T> void resolveCall(Call<T> call, BiConsumer<T, Throwable> onCallFinished) {
    call.enqueue(new Callback<>() {
      @Override
      public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!response.isSuccessful()) {
          // Get the raw response body of an unsuccessful response as a string.
          String errorDetails = "No error details.";
          try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
              errorDetails = errorBody.string();
            }
          } catch (IOException ignored) {
          }
          // Construct a verbose error message.
          String errorMessage = String.format(Locale.getDefault(), "Non-200 response code (%d). Details: %s", response.code(), errorDetails);
          // Call the callback with the error.
          onCallFinished.accept(null, new RuntimeException(errorMessage));
          return;
        }

        // Successful response.
        T result = response.body();
        assert result != null;
        onCallFinished.accept(result, null);
      }

      @Override
      public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        onCallFinished.accept(null, t);
      }
    });
  }
}
