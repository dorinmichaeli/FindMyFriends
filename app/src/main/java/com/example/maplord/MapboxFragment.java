package com.example.maplord;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.api.MapLordApi;
import com.example.maplord.databinding.AnnotationViewBinding;
import com.example.maplord.services.LocationService;
import com.example.maplord.services.MapLordApiService;
import com.example.maplord.databinding.FragmentMapboxBinding;
import com.mapbox.maps.ViewAnnotationOptions;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;

import java.util.HashMap;

public class MapboxFragment extends Fragment {
  private FragmentMapboxBinding binding;
  private MapView mapView;
  private PointAnnotationManager pointAnnotationManager;
  private ViewAnnotationManager viewAnnotationManager;

  // Dependencies.
  private MapLordApiService apiService;
  private LocationService locationService;

  // We use this map to find the marker info for a given annotation.
  // This is used when deleting an annotation, as we need to know the
  // marker's id in order to delete it from the server.
  private final HashMap<PointAnnotation, AnnotationInfo> markerMap = new HashMap<>();

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    apiService = MapLordApp.get(this).getApiService();
    locationService = MapLordApp.get(this).getLocationService();

    binding = FragmentMapboxBinding.inflate(inflater, container, false);
    mapView = binding.mapView;
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initGoBackButton();
    initMapboxEverything();
  }

  private void initGoBackButton() {
    binding.buttonMapbox.setOnClickListener(view1 -> {
      NavHostFragment.findNavController(MapboxFragment.this)
        .navigate(R.id.action_MapboxFragment_to_MenuFragment);
    });
  }

  private void initMapboxEverything() {
    initMapVisualStyle();
    initPointAnnotationManager();
    initViewAnnotationManager();
    initCameraLocation();
    initPreExistingMarkers();
    initCreateMarkerOnClick();
  }

  private void initMapVisualStyle() {
    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
  }

  private void initPointAnnotationManager() {
    // Create the Point Annotation Manager that we'll use for
    // creating, deleting, and interacting with the markers.
    AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
    assert annotationPlugin != null;

    pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, (AnnotationConfig) null);
    // Delete a marker when a user clicks on it.
    pointAnnotationManager.addClickListener(annotation -> {
      // Look up the marker info to know its id.
      AnnotationInfo info = markerMap.get(annotation);
      assert info != null;
      // Delete the marker in the API.
      LiveData<MapLordApi.MarkerDeletionResult> deletionResult = apiService.apiDeleteMarker(info.markerInfo);
      // Delete the equivalent annotation in MapBox.
      deletionResult.observe(getViewLifecycleOwner(), result -> {
        // Only delete if the server has deleted the marker.
        if (!result.deleted) {
          return;
        }
        // Delete the point annotation (marker image) from MapBox.
        pointAnnotationManager.delete(annotation);
        // Delete the view annotation (text label) from MapBox.
        viewAnnotationManager.removeViewAnnotation(info.viewAnnotation);
        // Delete the marker info from the HashMap.
        markerMap.remove(annotation);
      });
      return true;
    });
  }

  private void initViewAnnotationManager() {
    viewAnnotationManager = binding.mapView.getViewAnnotationManager();
  }

  private View addViewAnnotation(Point point, String text) {
    var options = new ViewAnnotationOptions.Builder()
      .geometry(point)
      .allowOverlap(true)
      .build();
    var view = viewAnnotationManager
      .addViewAnnotation(R.layout.annotation_view, options);
    TextView label = view.findViewById(R.id.annotation_text_label);
    label.setText(text);
    AnnotationViewBinding.bind(view);
    return view;
  }

  private void initCameraLocation() {
    Location location = locationService.getLastKnownLocation();
    Point newCameraLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());
    MapboxMap map = mapView.getMapboxMap();
    double cameraZoom = ResourceTools.getDouble(this, R.string.mapbox_default_camera_zoom);
    moveCameraTo(map, newCameraLocation, cameraZoom);
  }

  private void initPreExistingMarkers() {
    var markerList = apiService.getPreExistingMarkers();
    for (MapLordApi.MarkerInfo marker : markerList) {
      createAnnotationForMarker(marker);
    }
  }

  private void initCreateMarkerOnClick() {
    // Add a marker on the map wherever a user clicks.
    GesturesPlugin gesturesPlugin = GesturesUtils.getGestures(mapView);

    gesturesPlugin.addOnMapClickListener(point -> {
      LiveData<MapLordApi.MarkerInfo> createdMarkerInfo = apiService.apiCreateMarker(point);

      createdMarkerInfo.observe(getViewLifecycleOwner(), marker -> {
        // Create a visual annotation for the marker created by the API.
        createAnnotationForMarker(marker);
      });

      return true;
    });
  }

  private void createAnnotationForMarker(MapLordApi.MarkerInfo marker) {
    // Get the marker image.
    Drawable markerSource = AppCompatResources.getDrawable(requireContext(), R.drawable.red_marker);
    assert markerSource != null;
    assert markerSource instanceof BitmapDrawable;

    // Configure the point annotation options.
    double markerScale = ResourceTools.getDouble(this, R.string.mapbox_red_marker_scale);
    Point point = Point.fromLngLat(marker.lon, marker.lat);
    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
      .withPoint(point)
      .withIconImage(((BitmapDrawable) markerSource).getBitmap())
      .withIconSize(markerScale);

    // Create a point annotation for the marker image.
    PointAnnotation annotation = pointAnnotationManager.create(pointAnnotationOptions);

    // Create a view annotation for the text
    // label that shows the marker's owner.
    String markerOwner = sanitizeOwnerName(marker.owner);
    // Add an annotation for the marker owner.
    View view = addViewAnnotation(point, markerOwner);

    // Map the annotation info to the mapbox annotation.
    var info = new AnnotationInfo(marker, view);
    markerMap.put(annotation, info);
  }

  @NonNull
  private static String sanitizeOwnerName(@Nullable String ownerName) {
    if (ownerName == null || ownerName.isEmpty()) {
      return "unknown";
    }
    // Remove the ignored suffix from the marker owner name.
    // Example: "john.doe@gmail.com" -> "john.doe"
    final String ignoredSuffix = "@gmail.com";
    if (ownerName.endsWith(ignoredSuffix)) {
      ownerName = ownerName.substring(0, ownerName.length() - ignoredSuffix.length());
    }
    return ownerName;
  }

  private void moveCameraTo(MapboxMap map, Point point, double zoom) {
    CameraOptions camera = new CameraOptions.Builder()
      .zoom(zoom)
      .center(point)
      .build();

    map.setCamera(camera);
  }

  static class AnnotationInfo {
    public final MapLordApi.MarkerInfo markerInfo;
    public final View viewAnnotation;

    AnnotationInfo(MapLordApi.MarkerInfo markerInfo, View viewAnnotation) {
      this.markerInfo = markerInfo;
      this.viewAnnotation = viewAnnotation;
    }
  }
}
