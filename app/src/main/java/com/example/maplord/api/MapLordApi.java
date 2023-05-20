package com.example.maplord.api;

import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

public interface MapLordApi {
  @GET("/list-all-markers")
  Call<List<MarkerInfo>> listAllMarkers(); // [{"lat":...,"lon":...}, {...}]

  @POST("/create-marker")
  Call<MarkerInfo> createMarker(
    @Body MarkerCreationRequest markerInfo
  );

  @POST("/delete-marker")
  Call<MarkerDeletionResult> deleteMarker(
    @Body MarkerDeletionRequest deletionRequest
  );

  // Details of a single marker as reported by the API.
  class MarkerInfo {
    public String id;
    public String owner;
    public double lat;
    public double lon;
  }

  class MarkerCreationRequest {
    public final double lat;
    public final double lon;

    public MarkerCreationRequest(Point point) {
      this.lat = point.latitude();
      this.lon = point.longitude();
    }
  }

  class MarkerDeletionRequest {
    public String id;
  }

  class MarkerDeletionResult {
    public boolean deleted;
  }
}
