package com.example.maplord.api;

import com.example.maplord.model.MarkerInfo;

public class MarkerLocation {
  public double lat;
  public double lon;

  public MarkerLocation(MarkerInfo markerInfo) {
    this.lat = markerInfo.lat;
    this.lon = markerInfo.lon;
  }
}
