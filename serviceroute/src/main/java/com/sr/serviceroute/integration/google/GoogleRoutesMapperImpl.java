package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.service.planning.model.ResultadoPlanejamento;
import com.sr.serviceroute.service.planning.model.ResultadoWaypoint;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleRoutesMapperImpl implements GoogleRoutesMapper {

  @Override
  public GoogleRouteRequestDTO toGoogleRequest(
      Rota rota,
      List<RotaWaypoint> waypoints) {
    GoogleRouteRequestDTO request = new GoogleRouteRequestDTO();
    request.setTravelMode("DRIVE");
    request.setOptimizeWaypointOrder(true);

    request.setOrigin(toWaypoint(waypoints.get(0)));
    request.setDestination(toWaypoint(waypoints.get(waypoints.size() - 1)));

    request.setIntermediates(
        waypoints.subList(1, waypoints.size() - 1)
            .stream()
            .map(this::toWaypoint)
            .toList());

    request.setOptimizeWaypointOrder(false);
    request.setUnits("METRIC");

    // String[] fields = new String[] {
    // "routes.legs.durationMillis",
    // "routes.durationMillis",
    // "routes.optimizedIntermediateWaypointIndices",
    // "routes.distanceMeters" };

    // request.setFields(fields);

    return request;
  }

  private GoogleRouteRequestDTO.Waypoint toWaypoint(RotaWaypoint wp) {
    var latLng = new GoogleRouteRequestDTO.LatLng();
    latLng.setLatitude(wp.getWaypoint().getLatitude());
    latLng.setLongitude(wp.getWaypoint().getLongitude());

    var location = new GoogleRouteRequestDTO.Location();
    location.setLatLng(latLng);

    var waypoint = new GoogleRouteRequestDTO.Waypoint();
    waypoint.setLocation(location);

    return waypoint;
  }

  @Override
  public ResultadoPlanejamento fromGoogleResponse(
      GoogleRouteResponseDTO response) {
    GoogleRouteResponseDTO.Route route = response.getRoutes().get(0);

    ResultadoPlanejamento resultado = new ResultadoPlanejamento();
    resultado.setTempoTotal(
        Duration.ofMillis(route.getDurationMillis()));

    List<ResultadoWaypoint> resultados = new ArrayList<>();

    Instant acumulado = Instant.now();
    int seq = 0;

    // Origem
    acumulado = acumulado.plusMillis(route.getLegs().get(0).getDurationMillis());
    resultados.add(buildResultado(seq++, acumulado));

    // Intermediários (ordem otimizada)
    for (int i = 1; i < route.getLegs().size(); i++) {
      acumulado = acumulado.plusMillis(
          route.getLegs().get(i).getDurationMillis());
      resultados.add(buildResultado(seq++, acumulado));
    }

    resultado.setWaypoints(resultados);
    return resultado;
  }

  private ResultadoWaypoint buildResultado(int seq, Instant eta) {
    ResultadoWaypoint rw = new ResultadoWaypoint();
    rw.setSeq(seq);
    rw.setEtaPrevisto(eta);
    return rw;
  }

  // private GoogleRouteRequestDTO.Location toLocation(RotaWaypoint waypoint) {
  // GoogleRouteRequestDTO.Location location = new
  // GoogleRouteRequestDTO.Location();

  // location.setLat(waypoint.getWaypoint().getLatitude());
  // location.setLng(waypoint.getWaypoint().getLongitude());
  // return location;
  // }
}
