package com.example.maplord.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.maplord.MapLordApp;
import com.example.maplord.R;
import com.example.maplord.databinding.FragmentChatBinding;
import com.example.maplord.model.ChatMessage;
import com.example.maplord.services.ApiService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
  private static final String TAG = "ChatFragment";
  private static final int MAX_ITEMS = 30;

  private FragmentChatBinding binding;
  private ListView listView;
  private ArrayList<String> messageList;
  private ArrayAdapter<String> listAdapter;

  // Dependencies.
  private ApiService apiService;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    apiService = MapLordApp.get(this).getApiService();

    binding = FragmentChatBinding.inflate(inflater, container, false);

    // Get the list view.
    listView = binding.listView;
    // Create the underlying data source.
    messageList = new ArrayList<>();
    // Create the collection adapter.
    listAdapter = new ArrayAdapter<>(requireContext(), R.layout.chat_message_item, messageList);
    // Set the adapter.
    listView.setAdapter(listAdapter);

    // Enable the chat input functionality for sending messages.

    Button sendButton = binding.sendButton;
    EditText inputText = binding.inputText;

    sendButton.setOnClickListener(view -> {
      String newMessage = inputText.getText().toString();

      // If the message is empty, do nothing and return
      if (newMessage.isEmpty()) {
        return;
      }
      // Send the message to the server.
      apiService.sendChatMessage(newMessage);
      // Clear the input field
      inputText.getText().clear();

      // NOTE:
      //   We don't show the message in the list view here, because we will
      //   receive the message from the server in the onChatMessageReceived
      //   callback.
    });

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // TODO: Should listeners be here?

    apiService.onChatMessageReceived((chatMessage, isNew) -> {
      Log.d(TAG, "onChatMessageReceived");
      addChatMessageToListView(chatMessage);
    });
    apiService.onUserJoined((userJoined, isNew) -> {
      if (!isNew) {
        return;
      }
      Log.d(TAG, "onUserJoined");
      var message = String.format("User %s has joined the chat.", userJoined.userName);
      addItemToListView(message);
    });
    apiService.onUserLeft(userLeft -> {
      Log.d(TAG, "onUserLeft");
      var message = String.format("User %s has left the chat.", userLeft.userName);
      addItemToListView(message);
    });
    apiService.onMarkerAdded((newMarker, isNew) -> {
      if (!isNew) {
        return;
      }
      Log.d(TAG, "onMarkerAdded");
      String message = String.format("New marker created by %s", newMarker.owner);
      addItemToListView(message);
    });
    apiService.onMarkerDeleted(deletedMarker -> {
      Log.d(TAG, "onMarkerDeleted");
      String message = String.format("Marker deleted by %s", deletedMarker.owner);
      addItemToListView(message);
    });
  }

  private void addChatMessageToListView(ChatMessage chatMessage) {
    String userName = removeSuffix(chatMessage.userName, "@gmail.com");
    var message = String.format("[%s] %s: %s", makeMessageTimestamp(chatMessage.timestamp), userName, chatMessage.text);
    addItemToListView(message);
  }

  private void addItemToListView(String item) {
    requireActivity().runOnUiThread(() -> {
      // Add the item to the list.
      messageList.add(item);
      // Remove the first item if the list is too long.
      if (messageList.size() > MAX_ITEMS) {
        messageList.remove(0);
      }
      // Notify the adapter that the data source has changed.
      listAdapter.notifyDataSetChanged();
      // Scroll down to the last item.
      listView.setSelection(listAdapter.getCount() - 1);
    });
  }

  @SuppressLint("DefaultLocale")
  private static String makeMessageTimestamp(LocalDateTime date) {
    return String.format("%02d:%02d:%02d", date.getHour(), date.getMinute(), date.getSecond());
  }

  private static String removeSuffix(String source, String suffix) {
    if (source.endsWith(suffix)) {
      return source.substring(0, source.length() - suffix.length());
    }
    return source;
  }
}
