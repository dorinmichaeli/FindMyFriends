package com.example.findmyfriends.fragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.findmyfriends.App;
import com.example.findmyfriends.R;
import com.example.findmyfriends.components.MapDisplay;
import com.example.findmyfriends.model.MarkerInfo;
import com.example.findmyfriends.services.ApiService;
import com.example.findmyfriends.services.DialogService;
import com.example.findmyfriends.services.LocationService;
import com.example.findmyfriends.databinding.FragmentMapboxBinding;
import com.example.findmyfriends.services.UserService;
import com.example.findmyfriends.tools.ResourceTools;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;

public class MapboxFragment extends Fragment {
  private FragmentMapboxBinding binding;
  private MapDisplay mapDisplay;

  // Dependencies.
  private DialogService dialogService;
  private ApiService apiService;
  private UserService userService;
  private LocationService locationService;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    dialogService = App.get(this).getDialogService();
    apiService = App.get(this).getApiService();
    userService = App.get(this).getUserService();
    locationService = App.get(this).getLocationService();

    binding = FragmentMapboxBinding.inflate(inflater, container, false);
    mapDisplay = binding.mapDisplay;
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initMapboxEverything();

    apiService.onMarkerAdded((markerInfo, isNew) -> {
      requireActivity().runOnUiThread(() -> {
        // Create a visual annotation for the marker created by the API.
        placeMarkerOnMap(markerInfo);
      });
    });

    apiService.onMarkerDeleted(markerInfo -> {
      requireActivity().runOnUiThread(() -> {
        // Delete the visual annotation for the marker deleted by the API.
        deleteMarkerFromMap(markerInfo);
      });
    });
  }

  private void initMapboxEverything() {
    initMapVisualStyle();
    initDeleteOwnMarkerWhenClicked();
    initCreateMarkerOnMapClick();
    initCameraLocation();
  }

  private void initMapVisualStyle() {
    mapDisplay.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
  }

  private void initDeleteOwnMarkerWhenClicked() {
    mapDisplay.eventMarkerClicked.addListener(markerInfo -> {
      if (!userService.getUserEmail().equals(markerInfo.owner)) {
        // Can't delete a marker that you don't own.
        return;
      }

      // Delete the marker in the API.
      apiService.deleteMarker(markerInfo.id);
    });
  }

  private void initCameraLocation() {
    Location location = locationService.getLastKnownLocation();
    Point newCameraLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());
    MapboxMap map = mapDisplay.getMapboxMap();
    double cameraZoom = ResourceTools.getDouble(requireContext(), R.string.mapbox_default_camera_zoom);
    moveCameraTo(map, newCameraLocation, cameraZoom);
  }

  private void initCreateMarkerOnMapClick() {
    // Add a marker on the map wherever a user clicks.
    mapDisplay.eventMapClicked.addListener(point -> {
      dialogService.textInputPopup("Enter a title for your marker.", title -> {
        // Create a marker in the API.
        apiService.addMarker(title, point.latitude(), point.longitude());
      });
    });
  }

  private void placeMarkerOnMap(MarkerInfo markerInfo) {
    @DrawableRes int markerImage;
    if (markerInfo.owner.equalsIgnoreCase("admin")) {
      markerImage = R.drawable.marker_yellow;
    } else if (userService.getUserEmail().equals(markerInfo.owner)) {
      markerImage = R.drawable.marker_green;
    } else {
      markerImage = R.drawable.marker_red;
    }
    mapDisplay.createAnnotationForMarker(markerInfo, markerImage);
  }

  private void deleteMarkerFromMap(MarkerInfo markerInfo) {
    mapDisplay.deleteAnnotationForMarker(markerInfo);
  }

  private void moveCameraTo(MapboxMap map, Point point, double zoom) {
    CameraOptions camera = new CameraOptions.Builder()
      .zoom(zoom)
      .center(point)
      .build();

    map.setCamera(camera);
  }
}
