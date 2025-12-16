package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.model.enums.RotaWaypointTipo;
import com.sr.serviceroute.service.planning.model.ResultadoPlanejamento;
import com.sr.serviceroute.service.planning.model.ResultadoWaypoint;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GoogleRoutesMapperImpl implements GoogleRoutesMapper {

  @Override
  public GoogleRouteRequestDTO toGoogleRequest(
      Rota rota,
      List<RotaWaypoint> waypoints) {
    GoogleRouteRequestDTO request = new GoogleRouteRequestDTO();
    request.setTravelMode("DRIVE");
    request.setOptimizeWaypointOrder(true);

    Optional<RotaWaypoint> origin = waypoints.stream().filter(w -> w.getTipo() == RotaWaypointTipo.ORIGEM).findFirst();
    Optional<RotaWaypoint> destination = waypoints.stream().filter(w -> w.getTipo() == RotaWaypointTipo.DESTINO)
        .findFirst();

    if (!origin.isPresent() || !destination.isPresent()) {
      throw new IllegalArgumentException("Rota deve ter uma origem e um destino");
    }

    request.setOrigin(toWaypoint(origin.get()));
    request.setDestination(toWaypoint(destination.get()));

    request.setIntermediates(
        waypoints
            .stream()
            .filter(w -> w.getTipo() != RotaWaypointTipo.ORIGEM && w.getTipo() != RotaWaypointTipo.DESTINO)
            .map(this::toWaypoint)
            .toList());

    request.setOptimizeWaypointOrder(true);
    request.setUnits("METRIC");

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
        Duration.ofSeconds(convertDurationStringToLong(route.getDuration())));

    List<ResultadoWaypoint> resultados = new ArrayList<>();

    Instant acumulado = Instant.now();
    int seq = 0;

    // Origem
    acumulado = acumulado.plusSeconds(convertDurationStringToLong(route.getLegs().get(0).getDuration()));
    resultados.add(buildResultado(seq++, acumulado));

    // Intermediários (ordem otimizada)
    for (int i = 1; i < route.getLegs().size(); i++) {
      acumulado = acumulado.plusSeconds(convertDurationStringToLong(route.getLegs().get(i).getDuration()));
      resultados.add(buildResultado(seq++, acumulado));
    }

    resultado.setWaypoints(resultados);
    return resultado;
  }

  private long convertDurationStringToLong(String durationString) {
    // Verifica se a string termina com 's' e a remove.
    if (durationString != null && durationString.endsWith("s")) {
      String numericString = durationString.substring(0, durationString.length() - 1);

      try {
        // Usa Long.parseLong() para converter a parte numérica para long.
        return Long.parseLong(numericString);
      } catch (NumberFormatException e) {
        // Captura exceções se a parte numérica não for um long válido.
        throw new NumberFormatException("A parte numérica da string '" + durationString + "' é inválida.");
      }
    } else {
      throw new NumberFormatException("A string de duração não está no formato esperado (ex: '200s').");
    }
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
