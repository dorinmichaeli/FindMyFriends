package com.example.maplord;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.maplord.databinding.FragmentMapboxBinding;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

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
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
    }
}