package com.example.findmyfriends.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.findmyfriends.R;
import com.example.findmyfriends.databinding.AnnotationViewBinding;
import com.example.findmyfriends.model.MarkerInfo;
import com.example.findmyfriends.tools.EventHandler;
import com.mapbox.geojson.Point;
import com.mapbox.maps.MapView;
import com.mapbox.maps.ViewAnnotationOptions;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;

import java.util.HashMap;

public class MapDisplay extends MapView {
  private static final String TAG = "MapDisplay";

  // We use this map to find the marker info for a given annotation.
  // This is used when deleting an annotation, as we need to know the
  // marker's id in order to delete it from the server.
  // [find marker id from annotation]
  private final HashMap<PointAnnotation, AnnotationInfo> annotationMap = new HashMap<>();
  // We use this map to find the annotation for a given marker id.
  // [find annotation from marker id]
  private final HashMap<String, AnnotationInfo> markerIdMap = new HashMap<>();

  private PointAnnotationManager pointAnnotationManager;
  private ViewAnnotationManager viewAnnotationManager;

  // TODO: Event fields should probably not be public.
  //
  // Events
  //
  public final EventHandler<MarkerInfo> eventMarkerClicked = new EventHandler<>();
  public final EventHandler<Point> eventMapClicked = new EventHandler<>();

  public MapDisplay(@NonNull Context context) {
    super(context);
    init();
  }

  public MapDisplay(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MapDisplay(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initPointAnnotationManager();
    initViewAnnotationManager();
    initMapClickEvent();
  }

  public void createAnnotationForMarker(MarkerInfo marker, @DrawableRes int markerImage) {
    // Get the marker image.
    Drawable markerSource = AppCompatResources.getDrawable(getContext(), markerImage);
    assert markerSource != null;
    assert markerSource instanceof BitmapDrawable;

    DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
    float markerScale = display.density / 2;

    // Configure the point annotation options.
    Point point = Point.fromLngLat(marker.lon, marker.lat);
    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
      .withPoint(point)
      .withIconImage(((BitmapDrawable) markerSource).getBitmap())
      .withIconSize(markerScale);

    // Create a point annotation for the marker image.
    PointAnnotation annotation = pointAnnotationManager.create(pointAnnotationOptions);

    // Create a view annotation for the text

    // Add an annotation for the marker owner.
    View view = addViewAnnotation(point, marker.title);

    // Add the marker to our hashmaps.
    var info = new AnnotationInfo(annotation, marker, view);
    annotationMap.put(annotation, info);
    markerIdMap.put(marker.id, info);
  }

  public void deleteAnnotationForMarker(MarkerInfo markerInfo) {
    // Get the annotation for the marker that was deleted.
    AnnotationInfo info = markerIdMap.get(markerInfo.id);
    if (info == null) {
      // No annotation for this marker id, not much we can do.
      Log.d(TAG, "Attempted to delete non-existent marker: " + markerInfo.id);
      return;
    }
    PointAnnotation annotation = info.pointAnnotation;

    // Only delete if the server has deleted the marker.
    // Delete the point annotation (marker image) from MapBox.
    pointAnnotationManager.delete(annotation);
    // Delete the view annotation (text label) from MapBox.
    viewAnnotationManager.removeViewAnnotation(info.viewAnnotation);
    // Delete the relevant entries from our hashmaps.
    annotationMap.remove(annotation);
    markerIdMap.remove(markerInfo.id);
  }

  private void initPointAnnotationManager() {
    // Create the Point Annotation Manager that we'll use for
    // creating, deleting, and interacting with the markers.
    AnnotationPlugin annotationPlugin = getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
    assert annotationPlugin != null;

    pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, (AnnotationConfig) null);
    // Delete a marker when a user clicks on it.
    pointAnnotationManager.addClickListener(annotation -> {
      // Look up the marker info to know its id.
      AnnotationInfo info = annotationMap.get(annotation);
      assert info != null;
      eventMarkerClicked.notifyListeners(info.markerInfo);
      return true;
    });
  }

  private void initViewAnnotationManager() {
    viewAnnotationManager = getViewAnnotationManager();
  }

  private void initMapClickEvent() {
    // Add a marker on the map wherever a user clicks.
    GesturesPlugin gesturesPlugin = GesturesUtils.getGestures(this);

    gesturesPlugin.addOnMapClickListener(point -> {
      eventMapClicked.notifyListeners(point);
      return true;
    });
  }

  private View addViewAnnotation(Point point, String text) {
    // TODO: The following lines throw an error if lat/lon are not in range. Consider handling that error somehow?

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

  private static class AnnotationInfo {
    public final PointAnnotation pointAnnotation;
    public final MarkerInfo markerInfo;
    public final View viewAnnotation;

    AnnotationInfo(PointAnnotation pointAnnotation, MarkerInfo markerInfo, View viewAnnotation) {
      this.pointAnnotation = pointAnnotation;
      this.markerInfo = markerInfo;
      this.viewAnnotation = viewAnnotation;
    }
  }
}
