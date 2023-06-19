package com.example.maplord.tools;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class EventHandler<T> {
  private final ArrayList<Consumer<T>> listeners = new ArrayList<>();

  public void addListener(Consumer<T> listener) {
    Objects.requireNonNull(listener);
    listeners.add(listener);
  }

  public void notifyListeners(T event) {
    for (Consumer<T> listener : listeners) {
      listener.accept(event);
    }
  }
}
