package com.example.findmyfriends.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.example.findmyfriends.api.EventCreateResponse;
import com.example.findmyfriends.api.MarkerLocation;
import com.example.findmyfriends.components.MapDisplay;
import com.example.findmyfriends.model.MarkerInfo;
import com.example.findmyfriends.services.DialogService;
import com.example.findmyfriends.services.LocationService;
import com.example.findmyfriends.services.RestApiService;
import com.example.findmyfriends.tools.ResourceTools;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapboxMap;

import java.util.HashMap;
import java.util.UUID;

public class EventCreateActivity extends AppCompatActivity {
  private final HashMap<String, MarkerInfo> markers = new HashMap<>();

  private MapDisplay mapDisplay;
  private EditText textInput;

  // Dependencies.
  private DialogService dialogService;
  private LocationService locationService;
  private RestApiService restApiService;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_event_create);

    dialogService = App.get(this).getDialogService();
    locationService = App.get(this).getLocationService();
    restApiService = App.get(this).getRestApiService();

    mapDisplay = findViewById(R.id.map_display);
    Button button = findViewById(R.id.button_finish_creating_event);
    textInput = findViewById(R.id.edit_text_event_name);

    initCameraLocation();

    mapDisplay.eventMapClicked.addListener(point -> {
      dialogService.textInputPopup("Enter a title for your marker.", title -> {
        createMarkerFromPoint(title, point);
      });
    });
    mapDisplay.eventMarkerClicked.addListener(this::deleteMarkerFromInfo);

    button.setOnClickListener(v -> {
      tryCreateEvent();
    });
  }

  private void initCameraLocation() {
    Location location = locationService.getLastKnownLocation();
    Point newCameraLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());
    MapboxMap map = mapDisplay.getMapboxMap();
    double cameraZoom = ResourceTools.getDouble(this, R.string.mapbox_default_camera_zoom);
    moveCameraTo(map, newCameraLocation, cameraZoom);
  }

  private void moveCameraTo(MapboxMap map, Point point, double zoom) {
    CameraOptions camera = new CameraOptions.Builder()
      .zoom(zoom)
      .center(point)
      .build();

    map.setCamera(camera);
  }

  private void tryCreateEvent() {
    String eventName = textInput.getText().toString();
    if (eventName.isEmpty()) {
      dialogService.alert("Cannot create event", "Event name cannot be empty.", null);
      return;
    }

    if (this.markers.isEmpty()) {
      dialogService.alert("Cannot create event", "You must place at least one marker.", null);
      return;
    }

    var markerLocations = markers.values()
      .stream()
      .map(MarkerLocation::new)
      .toArray(MarkerLocation[]::new);

    // Create a new event.
    restApiService.createNewEvent(eventName, markerLocations, this::handleEventCreateApiResponse);
  }

  private void handleEventCreateApiResponse(EventCreateResponse newEvent, Throwable err) {
    if (err != null) {
      dialogService.alert("Failed to create event", "Something went wrong, please try again.", null);
      return;
    }

    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("eventId", newEvent.eventId);
    clipboard.setPrimaryClip(clip);

    String message = String.format(
      "Event \"%s\" created successfully. Your event id is: %s. It has been copied to your clipboard!",
      newEvent.eventName, newEvent.eventId);
    dialogService.alert("Event created successfully!", message, () -> {
      finishCreatingEvent();
    });
  }

  private void createMarkerFromPoint(String title, Point point) {
    // Create a new marker.
    var markerInfo = new MarkerInfo();
    markerInfo.id = UUID.randomUUID().toString();
    markerInfo.owner = "admin"; // TODO: Is this unused?
    markerInfo.title = title;
    markerInfo.lat = point.latitude();
    markerInfo.lon = point.longitude();
    // Add the marker to our list for later use
    // when the user finishes creating the event.
    markers.put(markerInfo.id, markerInfo);
    // Add the marker to the map for the user to see it.
    mapDisplay.createAnnotationForMarker(markerInfo, R.drawable.marker_yellow);
  }

  private void deleteMarkerFromInfo(MarkerInfo markerInfo) {
    // Remove the marker from our list of placed markers.
    markers.remove(markerInfo.id);
    // Remove the marker from the map.
    mapDisplay.deleteAnnotationForMarker(markerInfo);
  }

  private void finishCreatingEvent() {
    var intent = new Intent(EventCreateActivity.this, TitleMenuActivity.class);
    startActivity(intent);
  }
}
