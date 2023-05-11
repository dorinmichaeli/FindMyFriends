package com.example.maplord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;

import com.example.maplord.services.LocationService;
import com.example.maplord.databinding.FragmentLoaderBinding;
import com.example.maplord.services.MapLordApiService;

public class LoaderFragment extends Fragment {
  private boolean locationUpdated = false;
  private boolean markerListLoaded = false;

  // Dependencies.
  private MapLordApiService apiService;
  private LocationService locationService;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    apiService = MapLordApp.get(this).getApiService();
    locationService = MapLordApp.get(this).getLocationService();

    FragmentLoaderBinding binding = FragmentLoaderBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    startLoadingLastKnownLocation();
    startLoadingPreExistingMarkers();
  }

  private void startLoadingLastKnownLocation() {
    LiveData<Boolean> locationUpdated = locationService.updateLastKnownLocation();
    locationUpdated.observe(getViewLifecycleOwner(), updated -> {
      if (!updated) {
        return;
      }
      this.locationUpdated = true;
      checkIfEverythingFinished();
    });
  }

  private void startLoadingPreExistingMarkers() {
    LiveData<Boolean> markersUpdated = apiService.updatePreExistingMarkers();
    markersUpdated.observe(getViewLifecycleOwner(), updated -> {
      if (!updated) {
        return;
      }
      this.markerListLoaded = true;
      checkIfEverythingFinished();
    });
  }

  private void checkIfEverythingFinished() {
    if (locationUpdated && markerListLoaded) {
      NavHostFragment.findNavController(LoaderFragment.this)
        .navigate(R.id.action_LoaderFragment_to_MapboxFragment);
    }
  }
}
