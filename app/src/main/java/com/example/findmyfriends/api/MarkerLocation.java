package com.example.findmyfriends.api;

import com.example.findmyfriends.model.MarkerInfo;

public class MarkerLocation {
  public String title;
  public double lat;
  public double lon;

  public MarkerLocation(MarkerInfo markerInfo) {
    this.title = markerInfo.title;
    this.lat = markerInfo.lat;
    this.lon = markerInfo.lon;
  }
}
