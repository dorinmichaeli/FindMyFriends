package com.example.maplord.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.maplord.api.MapLordApi;
import com.mapbox.geojson.Point;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapLordApiService {
  private final MapLordApi api;
  private final DialogService dialogService;
  private List<MapLordApi.MarkerInfo> preExistingMarkers = null;

  public MapLordApiService(MapLordApi api, DialogService dialogService) {
    this.api = api;
    this.dialogService = dialogService;
  }

  public LiveData<Boolean> updatePreExistingMarkers() {
    var data = new MutableLiveData<>(false);

    Call<List<MapLordApi.MarkerInfo>> call = api.listAllMarkers();
    resolveCall(call, (call1, response, err) -> {
      if (err != null) {
        dialogService.fatalError("Error while sending list markers request: " + err);
        return;
      }

      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          dialogService.fatalError("Error while receiving list markers response: " + errBody);
        }
        return;
      }

      List<MapLordApi.MarkerInfo> markerList = response.body();
      assert markerList != null;
      preExistingMarkers = markerList;
      data.setValue(true);
    });

    return data;
  }

  public List<MapLordApi.MarkerInfo> getPreExistingMarkers() {
    assert preExistingMarkers != null;
    return preExistingMarkers;
  }

  public LiveData<MapLordApi.MarkerInfo> apiCreateMarker(Point point) {
    MutableLiveData<MapLordApi.MarkerInfo> data = new MutableLiveData<>();

    MapLordApi.MarkerCreationRequest creationRequest
      = new MapLordApi.MarkerCreationRequest(point);
    Call<MapLordApi.MarkerInfo> call = api.createMarker(creationRequest);
    resolveCall(call, (call1, response, err) -> {
      if (err != null) {
        dialogService.fatalError("Error while sending create marker request: " + err);
        return;
      }

      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          dialogService.fatalError("Error while receiving create marker response: " + errBody);
        }
        return;
      }

      MapLordApi.MarkerInfo createdMarkerInfo = response.body();
      assert createdMarkerInfo != null;
      data.setValue(createdMarkerInfo);
    });

    return data;
  }

  public LiveData<MapLordApi.MarkerDeletionResult> apiDeleteMarker(MapLordApi.MarkerInfo markerInfo) {
    MutableLiveData<MapLordApi.MarkerDeletionResult> data = new MutableLiveData<>();

    // Send the deletion request to the API.
    MapLordApi.MarkerDeletionRequest deletionRequest = new MapLordApi.MarkerDeletionRequest();
    deletionRequest.id = markerInfo.id;
    Call<MapLordApi.MarkerDeletionResult> call = api.deleteMarker(deletionRequest);
    resolveCall(call, (call1, response, err) -> {
      if (err != null) {
        dialogService.fatalError("Error while sending delete marker request: " + err);
        return;
      }

      if (!response.isSuccessful()) {
        try (ResponseBody errBody = response.errorBody()) {
          dialogService.fatalError("Error while receiving delete marker response: " + errBody);
        }
        return;
      }

      MapLordApi.MarkerDeletionResult result = response.body();
      assert result != null;
      data.setValue(result);
    });

    return data;
  }

  private static <T> void resolveCall(Call<T> call, CallFinished<T> onCallFinished) {
    call.enqueue(new Callback<>() {
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
}