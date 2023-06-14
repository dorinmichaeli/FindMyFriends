package com.example.maplord.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.maplord.model.ChatMessage;
import com.example.maplord.model.MarkerAddRequest;
import com.example.maplord.model.MarkerDeleteRequest;
import com.example.maplord.model.MarkerInfo;
import com.example.maplord.model.UserJoined;
import com.example.maplord.model.UserLeft;
import com.example.maplord.model.WelcomeMessage;
import com.example.maplord.tools.Action;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ApiService {
  private static final String TAG = "WsApiService";

  private final Gson gson;
  private final WebSocket socket;

  // TODO: These should probably not be on the API itself.
  private List<ChatMessage> loadedMessages = null;
  private List<MarkerInfo> loadedMarkers = null;

  public ApiService(String websocketUrl, String authToken, String groupId) {
    gson = new GsonBuilder()
      // This fancy function allows us to deserialize ISO date-time strings into LocalDateTime objects.
      .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
        // Get the timestamp as a string.
        String timestamp = json.getAsJsonPrimitive().getAsString();
        // Parse the timestamp as an ISO date-time string.
        var withOffset = ZonedDateTime
          .parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
          // Convert to the current time zone.
          .withZoneSameInstant(ZoneId.systemDefault());
        // Convert to a LocalDateTime.
        return withOffset.toLocalDateTime();
      })
      .create();

    var client = new OkHttpClient.Builder()
      .build();


    // Make sure to escape the query parameters.
    authToken = encodeURIComponent(authToken);
    groupId = encodeURIComponent(groupId);
    // Construct the full URL.
    String fullUrl = String.format("%s?authToken=%s&groupId=%s", websocketUrl, authToken, groupId);

    var request = new Request.Builder()
      .url(fullUrl)
      .build();

    Log.d(TAG, "Connecting to server...");
    socket = client.newWebSocket(request, new WsApiListener());
  }

  public List<ChatMessage> getLoadedMessages() {
    assert loadedMessages != null;
    return loadedMessages;
  }

  public List<MarkerInfo> getLoadedMarkers() {
    assert loadedMarkers != null;
    return loadedMarkers;
  }

  public void sendChatMessage(String messageText) {
    String messageJson = gson.toJson(messageText, String.class);
    socket.send(ClientMessageType.CHAT_MESSAGE + messageJson);
  }

  public void addMarker(double lat, double lon) {
    var addRequest = new MarkerAddRequest();
    addRequest.lat = lat;
    addRequest.lon = lon;
    String messageJson = gson.toJson(addRequest, MarkerAddRequest.class);
    socket.send(ClientMessageType.ADD_MARKER + messageJson);
  }

  public void deleteMarker(String markerId) {
    var deleteRequest = new MarkerDeleteRequest();
    deleteRequest.markerId = markerId;
    String messageJson = gson.toJson(deleteRequest, MarkerDeleteRequest.class);
    socket.send(ClientMessageType.DELETE_MARKER + messageJson);
  }

  public void close() {
    socket.close(1000, "User closed the app");
  }

  private void processMessageFromServer(char messageType, String messageJson) {
    switch (messageType) {
      case ServerMessageType.WELCOME:
        handleWelcome(messageJson);
        break;
      case ServerMessageType.ERROR:
        handleErrorReportedByServer(messageJson);
        break;
      case ServerMessageType.CHAT_MESSAGE:
        handleChatMessageReceived(messageJson);
        break;
      case ServerMessageType.USER_JOINED:
        handleUserJoined(messageJson);
        break;
      case ServerMessageType.USER_LEFT:
        handleUserLeft(messageJson);
        break;
      case ServerMessageType.MARKER_CREATED:
        handleMarkerCreated(messageJson);
        break;
      case ServerMessageType.MARKER_DELETED:
        handleMarkerDeleted(messageJson);
        break;
      default:
        Log.e(TAG, "Unknown message type received from WS API server: " + messageType);
        break;
    }
  }

  private class WsApiListener extends WebSocketListener {
    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
      Log.d(TAG, "Connected to server, now waiting for welcome message...");
      notifyConnectedToServer();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, String text) {
      Log.d(TAG, "onMessage: " + text);
      char messageType = text.charAt(0);
      String messageJson = text.substring(1);
      processMessageFromServer(messageType, messageJson);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
      Log.d(TAG, "Socket closed: " + reason);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
      Log.d(TAG, "Socket failure: " + t.getMessage());
      notifyErrorReportedByServer(t);
    }
  }

  private static class ServerMessageType {
    public static final char WELCOME = 'A';
    public static final char ERROR = 'B';
    public static final char CHAT_MESSAGE = 'C';
    public static final char USER_JOINED = 'D';
    public static final char USER_LEFT = 'E';
    public static final char MARKER_CREATED = 'F';
    public static final char MARKER_DELETED = 'G';
  }

  private static class ClientMessageType {
    public static final char CHAT_MESSAGE = 'z';
    public static final char ADD_MARKER = 'y';
    public static final char DELETE_MARKER = 'x';
  }

  private static String encodeURIComponent(String str) {
    // This is a Java port of the JavaScript encodeURIComponent function.
    // Taken from here: https://stackoverflow.com/a/611117

    try {
      // The better choice here would be to use StandardCharsets.UTF_8
      // without the toString() call, but that requires API level 33 for
      // whatever reason - so we just suppress the warning.
      //
      // noinspection CharsetObjectCanBeUsed
      return URLEncoder.encode(str, StandardCharsets.UTF_8.toString())
        .replaceAll("\\+", "%20")
        .replaceAll("%21", "!")
        .replaceAll("%27", "'")
        .replaceAll("%28", "(")
        .replaceAll("%29", ")")
        .replaceAll("%7E", "~");
    } catch (UnsupportedEncodingException e) {
      // Should never happen (?)
      throw new RuntimeException(e);
    }
  }

  // ======================================================
  // Event handling.
  // WARNING: Lots of repetition below.
  // ======================================================

  // FIXME: Add event listener lifecycle management to avoid resource leaks.

  //
  // Collections of event listeners.
  //

  private final ArrayList<Action> onConnectedToServer = new ArrayList<>();
  private final ArrayList<Consumer<WelcomeMessage>> onWelcome = new ArrayList<>();
  private final ArrayList<Consumer<Throwable>> onErrorReportedByServer = new ArrayList<>();
  private final ArrayList<Consumer<ChatMessage>> onChatMessageReceived = new ArrayList<>();
  private final ArrayList<Consumer<UserJoined>> onUserJoined = new ArrayList<>();
  private final ArrayList<Consumer<UserLeft>> onUserLeft = new ArrayList<>();
  private final ArrayList<Consumer<MarkerInfo>> onMarkerAdded = new ArrayList<>();
  private final ArrayList<Consumer<MarkerInfo>> onMarkerDeleted = new ArrayList<>();

  //
  // Process events before calling the event listeners.
  //

  private void handleWelcome(String messageJson) {
    Log.d(TAG, "Received welcome message from server: " + messageJson);
    WelcomeMessage welcomeMessage = gson.fromJson(messageJson, WelcomeMessage.class);

    // TODO: Do the next 2 lines outside!
    loadedMessages = Arrays.asList(welcomeMessage.chatHistory);
    loadedMarkers = Arrays.asList(welcomeMessage.markerList);

    notifyWelcomeMessage(welcomeMessage);
  }

  private void handleErrorReportedByServer(String messageJson) {
    Log.d(TAG, "Error reported by server: " + messageJson);
    String data = gson.fromJson(messageJson, String.class);
    notifyErrorReportedByServer(new Exception(data));
  }

  private void handleChatMessageReceived(String messageJson) {
    Log.d(TAG, "Chat message: " + messageJson);
    ChatMessage data = gson.fromJson(messageJson, ChatMessage.class);
    notifyChatMessageReceived(data);
  }

  private void handleUserJoined(String messageJson) {
    Log.d(TAG, "User joined: " + messageJson);
    UserJoined data = gson.fromJson(messageJson, UserJoined.class);
    notifyUserJoined(data);
  }

  private void handleUserLeft(String messageJson) {
    Log.d(TAG, "User left: " + messageJson);
    UserLeft data = gson.fromJson(messageJson, UserLeft.class);
    notifyUserLeft(data);
  }

  private void handleMarkerCreated(String messageJson) {
    Log.d(TAG, "Marker added: " + messageJson);
    MarkerInfo newMarker = gson.fromJson(messageJson, MarkerInfo.class);
    notifyMarkerAdded(newMarker);
  }

  private void handleMarkerDeleted(String messageJson) {
    Log.d(TAG, "Marker deleted: " + messageJson);
    MarkerInfo deletedMarker = gson.fromJson(messageJson, MarkerInfo.class);
    notifyMarkerDeleted(deletedMarker);
  }

  //
  // Notify event listeners of events.
  //

  private void notifyConnectedToServer() {
    for (var listener : onConnectedToServer) {
      listener.apply();
    }
  }

  private void notifyWelcomeMessage(WelcomeMessage welcomeMessage) {
    for (var listener : onWelcome) {
      listener.accept(welcomeMessage);
    }
  }

  private void notifyErrorReportedByServer(Throwable error) {
    for (var listener : onErrorReportedByServer) {
      listener.accept(error);
    }
  }

  private void notifyChatMessageReceived(ChatMessage message) {
    for (var listener : onChatMessageReceived) {
      listener.accept(message);
    }
  }

  private void notifyUserJoined(UserJoined userJoined) {
    for (var listener : onUserJoined) {
      listener.accept(userJoined);
    }
  }

  private void notifyUserLeft(UserLeft userLeft) {
    for (var listener : onUserLeft) {
      listener.accept(userLeft);
    }
  }

  private void notifyMarkerAdded(MarkerInfo newMarker) {
    for (var listener : onMarkerAdded) {
      listener.accept(newMarker);
    }
  }

  private void notifyMarkerDeleted(MarkerInfo deletedMarker) {
    for (var listener : onMarkerDeleted) {
      listener.accept(deletedMarker);
    }
  }

  //
  // Register event listeners.
  //

  public void onConnectedToServer(Action listener) {
    onConnectedToServer.add(listener);
  }

  public void onWelcomeMessage(Consumer<WelcomeMessage> listener) {
    onWelcome.add(listener);
  }

  public void onError(Consumer<Throwable> listener) {
    onErrorReportedByServer.add(listener);
  }

  public void onChatMessageReceived(Consumer<ChatMessage> listener) {
    onChatMessageReceived.add(listener);
  }

  public void onUserJoined(Consumer<UserJoined> listener) {
    onUserJoined.add(listener);
  }

  public void onUserLeft(Consumer<UserLeft> listener) {
    onUserLeft.add(listener);
  }

  public void onMarkerAdded(Consumer<MarkerInfo> listener) {
    onMarkerAdded.add(listener);
  }

  public void onMarkerDeleted(Consumer<MarkerInfo> listener) {
    onMarkerDeleted.add(listener);
  }
}
