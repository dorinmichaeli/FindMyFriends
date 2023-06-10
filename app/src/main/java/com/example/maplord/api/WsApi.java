package com.example.maplord.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.maplord.model.ChatMessage;
import com.example.maplord.model.UserJoined;
import com.example.maplord.model.UserLeft;
import com.example.maplord.services.UserService;
import com.example.maplord.tools.Action;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WsApi {
  private static final String TAG = "WsApiService";

  private final Gson gson = new Gson();
  private final WebSocket socket;

  private final UserService userService;

  // Events.
  // TODO: Add event listener lifecycle management to avoid resource leaks.
  private final ArrayList<Action> onAuthenticated = new ArrayList<>();
  private final ArrayList<Consumer<ChatMessage[]>> onHistory = new ArrayList<>();
  private final ArrayList<Consumer<UserJoined>> onUserJoined = new ArrayList<>();
  private final ArrayList<Consumer<ChatMessage>> onChatMessage = new ArrayList<>();
  private final ArrayList<Consumer<UserLeft>> onUserLeft = new ArrayList<>();

  public WsApi(String websocketUrl, UserService userService) {
    this.userService = userService;

    var client = new OkHttpClient.Builder()
      .build();

    var request = new Request.Builder()
      .url(websocketUrl)
      .build();

    Log.d(TAG, "Connecting to server...");
    socket = client.newWebSocket(request, new WsApiListener());
  }

  public void onAuthenticatedByServer(Action listener) {
    onAuthenticated.add(listener);
  }

  public void onHistoryReceived(Consumer<ChatMessage[]> listener) {
    onHistory.add(listener);
  }

  public void onUserJoined(Consumer<UserJoined> listener) {
    onUserJoined.add(listener);
  }

  public void onChatMessageReceived(Consumer<ChatMessage> listener) {
    onChatMessage.add(listener);
  }

  public void onUserLeft(Consumer<UserLeft> listener) {
    onUserLeft.add(listener);
  }

  public void sendMessage(String messageText) {
    String message = ClientMessageType.CHAT_MESSAGE + userService.getAuthTokenSync() + "::" + messageText;
    socket.send(message);
  }

  public void close() {
    socket.close(1000, "User closed the app");
  }

  private void notifyAuthenticatedByServer() {
    for (var listener : onAuthenticated) {
      listener.apply();
    }
  }

  private void notifyHistoryReceived(ChatMessage[] history) {
    for (var listener : onHistory) {
      listener.accept(history);
    }
  }

  private void notifyUserJoined(UserJoined userJoined) {
    for (var listener : onUserJoined) {
      listener.accept(userJoined);
    }
  }

  private void notifyChatMessageReceived(ChatMessage message) {
    for (var listener : onChatMessage) {
      listener.accept(message);
    }
  }

  private void notifyUserLeft(UserLeft userLeft) {
    for (var listener : onUserLeft) {
      listener.accept(userLeft);
    }
  }

  private void handleMessage(char messageType, String messageJson) {
    switch (messageType) {
      case ServerMessageType.USER_JOINED:
        handleUserJoined(messageJson);
        break;
      case ServerMessageType.CHAT_MESSAGE:
        handleChatMessage(messageJson);
        break;
      case ServerMessageType.USER_LEFT:
        handleUserLeft(messageJson);
        break;
      case ServerMessageType.CHAT_HISTORY:
        handleChatHistory(messageJson);
        break;
      case ServerMessageType.ERROR_REPORTED_BY_SERVER:
        handleErrorReportedByServer(messageJson);
        break;
      case ServerMessageType.AUTHENTICATED:
        handleAuthenticated(messageJson);
        break;
      default:
        Log.e(TAG, "Unknown message type received from WS API server: " + messageType);
        break;
    }
  }

  private void handleUserJoined(String messageJson) {
    UserJoined data = gson.fromJson(messageJson, UserJoined.class);
    notifyUserJoined(data);
    Log.d(TAG, "User joined: " + data.userName);
  }

  private void handleChatMessage(String messageJson) {
    ChatMessage data = gson.fromJson(messageJson, ChatMessage.class);
    notifyChatMessageReceived(data);
    Log.d(TAG, "Chat message: " + data.text);
  }

  private void handleUserLeft(String messageJson) {
    UserLeft data = gson.fromJson(messageJson, UserLeft.class);
    notifyUserLeft(data);
    Log.d(TAG, "User left: " + data.userName);
  }

  private void handleChatHistory(String messageJson) {
    ChatMessage[] data = gson.fromJson(messageJson, ChatMessage[].class);
    notifyHistoryReceived(data);
    Log.d(TAG, "Chat history: " + data.length + " messages");
  }

  private void handleErrorReportedByServer(String messageJson) {
    String data = gson.fromJson(messageJson, String.class);
    // TODO: Maybe show a dialog? Or just a toast message?
    Log.d(TAG, "Error reported by server: " + data);
  }

  private void handleAuthenticated(String messageJson) {
    assert messageJson.isEmpty();
    notifyAuthenticatedByServer();
    Log.d(TAG, "Authenticated with server!");
  }

  private void sendAuthenticateRequest() {
    String authenticationRequest = ClientMessageType.AUTHENTICATION_REQUEST + userService.getAuthTokenSync();
    socket.send(authenticationRequest);
  }

  private class WsApiListener extends WebSocketListener {
    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
      Log.d(TAG, "Connected to server, now trying to authenticate...");
      sendAuthenticateRequest();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, String text) {
      Log.d(TAG, "onMessage: " + text);
      char messageType = text.charAt(0);
      String messageJson = text.substring(1);
      handleMessage(messageType, messageJson);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
      Log.d(TAG, "Socket closed: " + reason);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
      Log.d(TAG, "Socket failure: " + t.getMessage());
    }
  }

  private static class ServerMessageType {
    public static final char USER_JOINED = 'A';
    public static final char CHAT_MESSAGE = 'B';
    public static final char USER_LEFT = 'C';
    public static final char CHAT_HISTORY = 'D';
    public static final char ERROR_REPORTED_BY_SERVER = 'E';
    public static final char AUTHENTICATED = 'F';
  }

  private static class ClientMessageType {
    public static final char AUTHENTICATION_REQUEST = 'z';
    public static final char CHAT_MESSAGE = 'y';
  }
}
