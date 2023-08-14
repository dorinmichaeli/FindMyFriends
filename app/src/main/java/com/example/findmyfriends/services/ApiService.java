package com.example.findmyfriends.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.findmyfriends.model.ChatMessage;
import com.example.findmyfriends.model.MarkerAddRequest;
import com.example.findmyfriends.model.MarkerDeleteRequest;
import com.example.findmyfriends.model.MarkerInfo;
import com.example.findmyfriends.model.UserInfo;
import com.example.findmyfriends.model.WelcomeMessage;
import com.example.findmyfriends.tools.Action;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * WebSocket API.
 */
public class ApiService {
  private static final String TAG = "WsApiService";
  private static final int WEBSOCKET_PING_INTERVAL_MS = 5_000;
  private static final int WEBSOCKET_RECONNECT_ATTEMPT_INTERVAL_MS = 5_000;
  private static final int CHAT_MESSAGES_MAX_BACKLOG = 100;

  private final String websocketUrl;
  private final Gson gson;
  private WebSocket socket;

  private List<ChatMessage> messages = null;
  private List<MarkerInfo> markers = null;
  private List<UserInfo> users = null;

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


    // Construct the full connection URL, which also includes the auth token
    // and the group id to connect to.
    authToken = encodeURIComponent(authToken);
    groupId = encodeURIComponent(groupId);
    this.websocketUrl = String.format("%s?authToken=%s&groupId=%s", websocketUrl, authToken, groupId);

    // Connect to the websocket API.
    createWebSocket();
  }

  private void reconnectWebSocket() {
    // Make sure the current socket is closed, before we get rid of the reference.
    socket.close(1000, "Reconnecting...");

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      createWebSocket();
    }, WEBSOCKET_RECONNECT_ATTEMPT_INTERVAL_MS);
  }

  private void createWebSocket() {
    Log.d(TAG, "Connecting to WebSocket API...");

    var client = new OkHttpClient.Builder()
      // Send pings periodically, otherwise the server may close the websocket
      // connection due to inactivity.
      .pingInterval(WEBSOCKET_PING_INTERVAL_MS, TimeUnit.MILLISECONDS)
      .retryOnConnectionFailure(true)
      .build();

    // Make sure to escape the query parameters.
    var request = new Request.Builder()
      .url(websocketUrl)
      .build();

    socket = client.newWebSocket(request, new WsApiListener());
  }

  public void sendChatMessage(String messageText) {
    String messageJson = gson.toJson(messageText, String.class);
    socket.send(ClientMessageType.ADD_CHAT_MESSAGE + messageJson);
  }

  public void addMarker(String title, double lat, double lon) {
    var addRequest = new MarkerAddRequest();
    addRequest.title = title;
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

  // TODO: Should this be called somewhere?
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
//      notifyConnectedToServer();
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, String text) {
      Log.d(TAG, "onMessage: " + text);
      try {
        char messageType = text.charAt(0);
        String messageJson = text.substring(1);
        processMessageFromServer(messageType, messageJson);
      } catch (Exception e) {
        Log.e(TAG, "Error while processing message from server: " + e.getMessage());
        notifyError(e);
      }
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
      Log.d(TAG, "Socket closed: " + reason);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
      Log.d(TAG, "Socket failure: " + t.getMessage());
      notifyError(t);
      // Try to reestablish the connection with the API.
      reconnectWebSocket();
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
    public static final char ADD_CHAT_MESSAGE = 'z';
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
  private final ArrayList<Consumer<Throwable>> onError = new ArrayList<>();
  private final ArrayList<BiConsumer<ChatMessage, Boolean>> onChatMessageReceived = new ArrayList<>();
  private final ArrayList<BiConsumer<UserInfo, Boolean>> onUserJoined = new ArrayList<>();
  private final ArrayList<Consumer<UserInfo>> onUserLeft = new ArrayList<>();
  private final ArrayList<BiConsumer<MarkerInfo, Boolean>> onMarkerAdded = new ArrayList<>();
  private final ArrayList<Consumer<MarkerInfo>> onMarkerDeleted = new ArrayList<>();

  //
  // Process events before calling the event listeners.
  //

  private void handleWelcome(String messageJson) {
    Log.d(TAG, "Received welcome message from server: " + messageJson);
    WelcomeMessage welcomeMessage = gson.fromJson(messageJson, WelcomeMessage.class);

    // TODO: Do the next 2 lines outside!
    messages = welcomeMessage.chatHistory;
    markers = welcomeMessage.markerList;
    users = welcomeMessage.usersInGroup;

    notifyConnectedToServer();
  }

  private void handleErrorReportedByServer(String messageJson) {
    Log.d(TAG, "Error reported by server: " + messageJson);
    String data = gson.fromJson(messageJson, String.class);
    notifyError(new Exception(data));
  }

  private void handleChatMessageReceived(String messageJson) {
    Log.d(TAG, "Chat message: " + messageJson);
    ChatMessage data = gson.fromJson(messageJson, ChatMessage.class);
    notifyChatMessageReceived(data);
  }

  private void handleUserJoined(String messageJson) {
    Log.d(TAG, "User joined: " + messageJson);
    UserInfo data = gson.fromJson(messageJson, UserInfo.class);
    notifyUserJoined(data);
  }

  private void handleUserLeft(String messageJson) {
    Log.d(TAG, "User left: " + messageJson);
    UserInfo data = gson.fromJson(messageJson, UserInfo.class);
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

  private void notifyError(Throwable error) {
    for (var listener : onError) {
      try {
        listener.accept(error);
      } catch (Exception e) {
        Log.e(TAG, "Error in error listener: " + e.getMessage());
      }
    }
  }

  private void notifyChatMessageReceived(ChatMessage message) {
    messages.add(message);
    if (messages.size() > CHAT_MESSAGES_MAX_BACKLOG) {
      messages.remove(0);
    }

    for (var listener : onChatMessageReceived) {
      listener.accept(message, true);
    }
  }

  private void notifyUserJoined(UserInfo userJoined) {
    users.add(userJoined);
    for (var listener : onUserJoined) {
      listener.accept(userJoined, true);
    }
  }

  private void notifyUserLeft(UserInfo userLeft) {
    users.removeIf(user -> user.userName.equals(userLeft.userName));
    for (var listener : onUserLeft) {
      listener.accept(userLeft);
    }
  }

  private void notifyMarkerAdded(MarkerInfo newMarker) {
    markers.add(newMarker);
    for (var listener : onMarkerAdded) {
      listener.accept(newMarker, true);
    }
  }

  private void notifyMarkerDeleted(MarkerInfo deletedMarker) {
    markers.removeIf(marker -> marker.id.equals(deletedMarker.id));
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

  public void onError(Consumer<Throwable> listener) {
    onError.add(listener);
  }

  public void onChatMessageReceived(BiConsumer<ChatMessage, Boolean> listener) {
    for (var message : messages) {
      listener.accept(message, false);
    }
    onChatMessageReceived.add(listener);
  }

  public void onUserJoined(BiConsumer<UserInfo, Boolean> listener) {
    for (var user : users) {
      listener.accept(user, false);
    }
    onUserJoined.add(listener);
  }

  public void onUserLeft(Consumer<UserInfo> listener) {
    onUserLeft.add(listener);
  }

  public void onMarkerAdded(BiConsumer<MarkerInfo, Boolean> listener) {
    for (var marker : markers) {
      listener.accept(marker, false);
    }
    onMarkerAdded.add(listener);
  }

  public void onMarkerDeleted(Consumer<MarkerInfo> listener) {
    onMarkerDeleted.add(listener);
  }
}
