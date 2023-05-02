package com.example.maplord;

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

    class MarkerInfo {
        public String id;
        public double lat;
        public double lon;
    }

    class MarkerCreationRequest {
        public MarkerCreationRequest(Point point) {
            this.lat = point.latitude();
            this.lon = point.longitude();
        }

        public double lat;
        public double lon;
    }

    class MarkerDeletionRequest {
        public String id;
    }

    class MarkerDeletionResult {
        public boolean deleted;
    }

}