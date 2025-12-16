package com.sr.serviceroute.integration.google.dto;

import lombok.Data;
import java.util.List;

@Data
public class GoogleRouteRequestDTO {

  private Waypoint origin;
  private Waypoint destination;
  private List<Waypoint> intermediates;

  private String travelMode; // DRIVE
  private boolean optimizeWaypointOrder;

  private String units;

  @Data
  public static class Waypoint {
    private Location location;
  }

  @Data
  public static class Location {
    private LatLng latLng;
  }

  @Data
  public static class LatLng {
    private double latitude;
    private double longitude;
  }
}
