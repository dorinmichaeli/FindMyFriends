package com.example.maplord.services;

import com.google.firebase.auth.FirebaseUser;

public class UserService {
  private FirebaseUser user;

  public boolean isLoggedIn() {
    return this.user != null;
  }

  public void setUser(FirebaseUser user) {
    assert !this.isLoggedIn();
    this.user = user;
  }

  public FirebaseUser getUser() {
    assert this.isLoggedIn();
    return this.user;
  }

  // TODO: Move login-related logic from LoginFragment to this class.
}
