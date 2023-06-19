package com.example.maplord.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maplord.R;
import com.example.maplord.components.MapDisplay;
import com.example.maplord.model.MarkerInfo;

import java.util.HashMap;
import java.util.UUID;

public class EventCreateActivity extends AppCompatActivity {
  private final HashMap<String, MarkerInfo> markers = new HashMap<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_event_create);

    MapDisplay mapDisplay = findViewById(R.id.map_display);

    mapDisplay.eventMapClicked.addListener(point -> {
      // Create a new marker.
      var markerInfo = new MarkerInfo();
      markerInfo.id = UUID.randomUUID().toString();
      markerInfo.owner = "event marker";
      markerInfo.lat = point.latitude();
      markerInfo.lon = point.longitude();
      // Add the marker to our list for later use
      // when the user finishes creating the event.
      markers.put(markerInfo.id, markerInfo);
      // Add the marker to the map for the user to see it.
      mapDisplay.createAnnotationForMarker(markerInfo, R.drawable.red_marker);
    });
    mapDisplay.eventMarkerClicked.addListener(markerInfo -> {
      // Remove the marker from our list of placed markers.
      markers.remove(markerInfo.id);
      // Remove the marker from the map.
      mapDisplay.deleteAnnotationForMarker(markerInfo);
    });
  }
}
