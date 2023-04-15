package com.example.maplord;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.databinding.FragmentMapboxBinding;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

public class MapboxFragment extends Fragment {
    private MapView mapView;
    private FragmentMapboxBinding binding;

    public static MapboxFragment newInstance() {
        return new MapboxFragment();
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentMapboxBinding.inflate(inflater, container, false);
        initMapView();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonMapbox.setOnClickListener(view1 -> {
            NavHostFragment.findNavController(MapboxFragment.this)
                .navigate(R.id.action_MapboxFragment_to_FirstFragment);
        });
    }

    private void initMapView() {
        mapView = binding.mapView;

        MapboxMap map = mapView.getMapboxMap();
        map.loadStyleUri(Style.MAPBOX_STREETS);

        // TODO: Remove the following hardcoded locations for the markers and the camera.

        addMarkerAt(Point.fromLngLat(34.7817676, 32.0852999));
        addMarkerAt(Point.fromLngLat(34.810240, 32.048996));
        moveCameraTo(map, Point.fromLngLat(34.7817676, 32.0852999), 13.0);
    }

    private void addMarkerAt(Point point) {
        AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        assert annotationPlugin != null;
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, (AnnotationConfig) null);

        Drawable markerSource = AppCompatResources.getDrawable(requireContext(), R.drawable.red_marker);
        assert markerSource != null;
        assert markerSource instanceof BitmapDrawable;

        double markerScale = ResourceTools.getDouble(this, R.string.mapbox_red_marker_scale);
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(((BitmapDrawable) markerSource).getBitmap())
            .withIconSize(markerScale);

        pointAnnotationManager.create(pointAnnotationOptions);
    }

    private void moveCameraTo(MapboxMap map, Point point, double zoom) {
        CameraOptions camera = new CameraOptions.Builder()
            .zoom(zoom)
            .center(point)
            .build();
        map.setCamera(camera);
    }
}