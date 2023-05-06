package com.example.maplord;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.databinding.FragmentMapboxBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.Annotation;
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
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapboxFragment extends Fragment {
  private FragmentMapboxBinding binding;
  private MapView mapView;
  private PointAnnotationManager pointAnnotationManager;
  private MapLordApi api;
  // We use this map to find the marker info for a given annotation.
  // This is used when deleting an annotation, as we need to know the
  // marker's id in order to delete it from the server.
  private final HashMap<PointAnnotation, MapLordApi.MarkerInfo> markerMap = new HashMap<>();

  public static MapboxFragment newInstance() {
    return new MapboxFragment();
  }

  @Override
  public View onCreateView(
    @NonNull LayoutInflater inflater,
    @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState
  ) {
    // TODO: Where's the best place to initialize the API?
    api = createMapLordApi();

    binding = FragmentMapboxBinding.inflate(inflater, container, false);
    initMapbox();
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonMapbox.setOnClickListener(view1 -> {
      NavHostFragment.findNavController(MapboxFragment.this)
        .navigate(R.id.action_MapboxFragment_to_FirstFragment);
    });

    // Add a marker on the map wherever a user clicks.
    GesturesPlugin gesturesPlugin = GesturesUtils.getGestures(mapView);
    gesturesPlugin.addOnMapClickListener(point -> {
      apiCreateMarker(point);
      return true;
    });
  }

  private void initMapbox() {
    // Cache the mapView element.
    mapView = binding.mapView;

    // Create the Point Annotation Manager that we'll use for
    // creating, deleting, and interacting with the markers.
    AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
    assert annotationPlugin != null;
    pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, (AnnotationConfig) null);
    // Delete a marker when a user clicks on it.
    pointAnnotationManager.addClickListener(annotation -> {
      apiDeleteMarkerByAnnotation(annotation);
      return true;
    });

    // Load the map style.
    MapboxMap map = mapView.getMapboxMap();
    map.loadStyleUri(Style.MAPBOX_STREETS);

    // TODO: Remove the following hardcoded locations for the markers and the camera.

    //addMarkerAt(Point.fromLngLat(34.7817676, 32.0852999));
    //addMarkerAt(Point.fromLngLat(34.810240, 32.048996));
    moveCameraTo(map, Point.fromLngLat(34.7817676, 32.0852999), 13.0);

    apiListMarkers().observe(getViewLifecycleOwner(), markerList -> {
      for (MapLordApi.MarkerInfo marker : markerList) {
        addMarkerAt(marker);
      }
    });
  }

  private void addMarkerAt(MapLordApi.MarkerInfo marker) {
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
    // TODO: Find out why retrofit doesn't clean up after itself.

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

  private static <T> void resolveCall(Call<T> call, CallFinished<T> onCallFinished) {
    call.enqueue(new Callback<T>() {
      @Override
      public void onResponse(Call<T> call, Response<T> response) {
        onCallFinished.apply(call, response, null);
      }

      @Override
      public void onFailure(Call<T> call, Throwable t) {
        onCallFinished.apply(call, null, t);
      }
    });
  }

  private interface CallFinished<T> {
    void apply(Call<T> call, Response<T> response, Throwable t);
  }

  // ========================================================================
  // API calls
  // ========================================================================

  private LiveData<List<MapLordApi.MarkerInfo>> apiListMarkers() {
    MutableLiveData<List<MapLordApi.MarkerInfo>> data = new MutableLiveData<>();

    Call<List<MapLordApi.MarkerInfo>> call = api.listAllMarkers();
    resolveCall(call, (call1, response, err) -> {
      // TODO: How to handle errors in communication with the server?
      if (err != null) {
        ErrorDialog.fatalError(getActivity(), "Error while sending list markers request: " + err);
        return;
      }
      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          ErrorDialog.fatalError(getActivity(), "Error while receiving list markers response: " + errBody);
        }
        return;
      }

      List<MapLordApi.MarkerInfo> markerList = response.body();
      assert markerList != null;
      data.setValue(markerList);
    });

    return data;
  }

  private void apiCreateMarker(Point point) {
    MapLordApi.MarkerCreationRequest creationRequest
      = new MapLordApi.MarkerCreationRequest(point);
    Call<MapLordApi.MarkerInfo> call = api.createMarker(creationRequest);
    resolveCall(call, (call1, response, err) -> {
      // TODO: How to handle errors in communication with the server?
      if (err != null) {
        ErrorDialog.fatalError(getActivity(), "Error while sending create marker request: " + err);
        return;
      }
      // lat: ValidatorError: Path `lat` is required.
      // lon: ValidatorError: Path `lon` is required
      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          ErrorDialog.fatalError(getActivity(), "Error while receiving create marker response: " + errBody);
        }
        return;
      }
      MapLordApi.MarkerInfo createdMarkerInfo = response.body();
      assert createdMarkerInfo != null;
      addMarkerAt(createdMarkerInfo);
    });
  }

  private void apiDeleteMarkerByAnnotation(PointAnnotation annotation) {
    // 1. Look up the marker info to know its id.
    MapLordApi.MarkerInfo markerInfo = markerMap.get(annotation);
    assert markerInfo != null;
    // 2. Send the deletion request to the API.
    MapLordApi.MarkerDeletionRequest deletionRequest = new MapLordApi.MarkerDeletionRequest();
    deletionRequest.id = markerInfo.id;
    Call<MapLordApi.MarkerDeletionResult> call = api.deleteMarker(deletionRequest);
    resolveCall(call, (call1, response, err) -> {
      // TODO: How to handle errors in communication with the server?
      if (err != null) {
        ErrorDialog.fatalError(getActivity(), "Error while sending delete marker request: " + err);
        return;
      }
      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          ErrorDialog.fatalError(getActivity(), "Error while receiving delete marker response: " + errBody);
        }
        return;
      }

      MapLordApi.MarkerDeletionResult result = response.body();
      assert result != null;
      // 3. Only delete if the server has deleted the marker.
      if (result.deleted) {
        // 4. Delete the annotation from MapBox.
        pointAnnotationManager.delete(annotation);
        // 5. Delete the marker info from the HashMap.
        markerMap.remove(annotation);
      }
    });
  }
}