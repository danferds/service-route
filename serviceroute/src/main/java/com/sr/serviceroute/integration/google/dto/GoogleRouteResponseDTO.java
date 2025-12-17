package com.sr.serviceroute.integration.google.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoogleRouteResponseDTO {

  /**
   * Lista de rotas retornadas pela Routes API
   */
  private List<Route> routes;

  @Data
  public static class Route {

    /**
     * Pernas da rota (entre waypoints)
     */
    private List<Leg> legs;

    /**
     * Distância total da rota em metros
     */
    private long distanceMeters;

    /**
     * Duração total da rota (ex: "14136s")
     */
    private String duration;

    /**
     * Polyline da rota completa
     */
    private Polyline polyline;

    /**
     * Ordem otimizada dos waypoints intermediários
     */
    private List<Integer> optimizedIntermediateWaypointIndex;
  }

  @Data
  public static class Leg {

    /**
     * Distância da perna em metros
     */
    private long distanceMeters;

    /**
     * Duração da perna (ex: "8820s")
     */
    private String duration;

    /**
     * Polyline da perna
     */
    // private Polyline polyline;

    /**
     * Localização inicial da perna
     */
    private Location startLocation;

    /**
     * Localização final da perna
     */
    private Location endLocation;
  }

  @Data
  public static class Polyline {

    /**
     * Polyline codificada
     */
    private String encodedPolyline;
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
