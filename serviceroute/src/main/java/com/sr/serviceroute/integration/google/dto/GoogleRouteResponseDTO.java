package com.sr.serviceroute.integration.google.dto;

import lombok.Data;
import java.util.List;

@Data
public class GoogleRouteResponseDTO {

  private List<Route> routes;

  @Data
  public static class Route {

    private long durationMillis;
    private long distanceMeters;

    private List<Leg> legs;

    private List<Integer> optimizedIntermediateWaypointIndices;
  }

  @Data
  public static class Leg {

    private long durationMillis;
    private long distanceMeters;
  }
}
