package com.example.maplord.services;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.concurrent.ExecutionException;

public class UserService {
  private FirebaseUser user;

  public boolean isLoggedIn() {
    return this.user != null;
  }

  public void setUser(FirebaseUser user) {
    assert !this.isLoggedIn();
    this.user = user;
  }

  public Task<String> getAuthToken() {
    return user.getIdToken(true).continueWith(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }
      return task.getResult().getToken();
    });
  }

  public String getAuthTokenSync() {
    Task<GetTokenResult> authTokenTask = user.getIdToken(false);
    // Map the task to a task that returns the token string.
    try {
      return Tasks.await(authTokenTask).getToken();
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public String getUserEmail() {
    assert this.isLoggedIn();
    return user.getEmail();
  }

  // TODO: Move login-related logic from LoginActivity to this class.
}
