package com.example.maplord;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.api.MapLordApi;
import com.example.maplord.api.MapLordModel;
import com.example.maplord.databinding.FragmentMapboxBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import java.util.HashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapboxFragment extends Fragment {
  private FragmentMapboxBinding binding;
  private MapView mapView;
  private PointAnnotationManager pointAnnotationManager;
  private MapLordModel model;
  // We use this map to find the marker info for a given annotation.
  // This is used when deleting an annotation, as we need to know the
  // marker's id in order to delete it from the server.
  private final HashMap<PointAnnotation, MapLordApi.MarkerInfo> markerMap = new HashMap<>();

  @Override
  public View onCreateView(
    @NonNull LayoutInflater inflater,
    @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState
  ) {
    // TODO: Where's the best place to initialize the API?
    MapLordApi api = createMapLordApi();
    model = new MapLordModel(api, getActivity());

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
        .navigate(R.id.action_MapboxFragment_to_FirstFragment);
    });
  }

  private void initMapboxEverything() {
    initMapVisualStyle();
    initPointAnnotationManager();
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
      MapLordApi.MarkerInfo markerInfo = markerMap.get(annotation);
      assert markerInfo != null;
      // Delete the marker in the API.
      LiveData<MapLordApi.MarkerDeletionResult> deletionResult = model.apiDeleteMarker(markerInfo);
      // Delete the equivalent annotation in MapBox.
      deletionResult.observe(getViewLifecycleOwner(), result -> {
        // Only delete if the server has deleted the marker.
        if (!result.deleted) {
          return;
        }
        // Delete the annotation from MapBox.
        pointAnnotationManager.delete(annotation);
        // Delete the marker info from the HashMap.
        markerMap.remove(annotation);
      });
      return true;
    });
  }

  private void initCameraLocation() {
    MainActivity mainActivity = (MainActivity) getActivity();
    assert mainActivity != null;
    LiveData<Location> locationData = mainActivity.getLastKnownLocation();

    locationData.observe(getViewLifecycleOwner(), location -> {
      Point newCameraLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());
      MapboxMap map = mapView.getMapboxMap();
      double cameraZoom = ResourceTools.getDouble(this, R.string.mapbox_default_camera_zoom);
      moveCameraTo(map, newCameraLocation, cameraZoom);
    });
  }

  private void initPreExistingMarkers() {
    model.apiListMarkers().observe(getViewLifecycleOwner(), markerList -> {
      for (MapLordApi.MarkerInfo marker : markerList) {
        createAnnotationForMarker(marker);
      }
    });
  }

  private void initCreateMarkerOnClick() {
    // Add a marker on the map wherever a user clicks.
    GesturesPlugin gesturesPlugin = GesturesUtils.getGestures(mapView);
    gesturesPlugin.addOnMapClickListener(point -> {
      LiveData<MapLordApi.MarkerInfo> createdMarkerInfo = model.apiCreateMarker(point);
      createdMarkerInfo.observe(getViewLifecycleOwner(), marker -> {
        createAnnotationForMarker(marker);
      });
      return true;
    });
  }

  private void createAnnotationForMarker(MapLordApi.MarkerInfo marker) {
    Drawable markerSource = AppCompatResources.getDrawable(requireContext(), R.drawable.red_marker);
    assert markerSource != null;
    assert markerSource instanceof BitmapDrawable;

    double markerScale = ResourceTools.getDouble(this, R.string.mapbox_red_marker_scale);
    Point point = Point.fromLngLat(marker.lon, marker.lat);
    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
      .withPoint(point)
      .withIconImage(((BitmapDrawable) markerSource).getBitmap())
      .withIconSize(markerScale);

    PointAnnotation annotation = pointAnnotationManager.create(pointAnnotationOptions);
    markerMap.put(annotation, marker);
  }

  private void moveCameraTo(MapboxMap map, Point point, double zoom) {
    CameraOptions camera = new CameraOptions.Builder()
      .zoom(zoom)
      .center(point)
      .build();

    map.setCamera(camera);
  }

  private static MapLordApi createMapLordApi() {
    Gson gson = new GsonBuilder()
      .create();

    OkHttpClient client = new OkHttpClient.Builder()
      .build();

    Retrofit retrofit = new Retrofit.Builder()
      // TODO: Export the URL to a resource file or whatever?
      .baseUrl("http://maplord.api:3000")
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build();

    MapLordApi api = retrofit.create(MapLordApi.class);

    return api;
  }
}