package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.model.enums.RotaWaypointTipo;
import com.sr.serviceroute.service.planning.model.ResultadoPlanejamento;
import com.sr.serviceroute.service.planning.model.ResultadoWaypoint;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
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
    log.error("Intermediarios: {}", request.getIntermediates());

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

    Instant acumulado = Instant.now();
    int seq = 0;

    // 1. Origem
    ResultadoWaypoint originResult = buildResultado(seq++, acumulado);

    // 2. Intermediários e Destino
    int numIntermediates = route.getLegs().size() - 1;
    List<ResultadoWaypoint> intermediateResults = new ArrayList<>(
        java.util.Collections.nCopies(numIntermediates, null));
    List<ResultadoWaypoint> unmappedIntermediates = new ArrayList<>();
    ResultadoWaypoint destResult = null;

    List<Integer> optimizedIndices = route.getOptimizedIntermediateWaypointIndex();
    if (optimizedIndices == null) {
      optimizedIndices = new ArrayList<>();
      for (int i = 0; i < numIntermediates; i++) {
        optimizedIndices.add(i);
      }
    }

    for (int i = 0; i < route.getLegs().size(); i++) {
      GoogleRouteResponseDTO.Leg leg = route.getLegs().get(i);
      acumulado = acumulado.plusSeconds(convertDurationStringToLong(leg.getDuration()));
      ResultadoWaypoint currentResult = buildResultado(seq++, acumulado);

      if (i < numIntermediates) {
        // É um ponto intermediário
        boolean mapped = false;
        if (i < optimizedIndices.size()) {
          int originalIndex = optimizedIndices.get(i);
          if (originalIndex >= 0 && originalIndex < intermediateResults.size()) {
            intermediateResults.set(originalIndex, currentResult);
            mapped = true;
            log.info("Mapping Leg {} (Seq {}) to Original Intermediate Index {}", i, currentResult.getSeq(),
                originalIndex);
          } else {
            log.warn("Invalid optimized index {} for Leg {}. Will use fallback.", originalIndex, i);
          }
        }

        if (!mapped) {
          unmappedIntermediates.add(currentResult);
        }
      } else {
        log.info("Leg {} é Destinado (Seq {})", i, currentResult.getSeq());
        destResult = currentResult;
      }
    }

    // Fallback: Preencher buracos com os não mapeados
    int unmappedIndex = 0;
    for (int i = 0; i < intermediateResults.size(); i++) {
      if (intermediateResults.get(i) == null) {
        if (unmappedIndex < unmappedIntermediates.size()) {
          intermediateResults.set(i, unmappedIntermediates.get(unmappedIndex++));
          log.info("Fallback: Slot {} com Resultado nao mapeado (Seq {})",
              i, intermediateResults.get(i).getSeq());
        } else {
          log.error("Critico: Falta resultado para o slot intermediario {}!", i);
        }
      }
    }

    List<ResultadoWaypoint> finalResults = new ArrayList<>();
    finalResults.add(originResult);
    finalResults.addAll(intermediateResults);
    if (destResult != null) {
      finalResults.add(destResult);
    }

    // Limpar nulos caso algo tenha falhado (paranoid check)
    finalResults.removeIf(java.util.Objects::isNull);

    log.info("Resultado mapeados: {}", finalResults.stream().map(r -> r == null ? "null" : r.getSeq()).toList());

    resultado.setWaypoints(finalResults);
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
  @Override
  public List<com.sr.serviceroute.model.RotaLeg> mapLegs(GoogleRouteResponseDTO response, Rota rota) {
    if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {
      return new ArrayList<>();
    }

    GoogleRouteResponseDTO.Route route = response.getRoutes().get(0);
    List<GoogleRouteResponseDTO.Leg> legs = route.getLegs();

    if (legs == null || legs.isEmpty()) {
      return new ArrayList<>();
    }

    List<com.sr.serviceroute.model.RotaLeg> rotaLegs = new ArrayList<>();
    for (int i = 0; i < legs.size(); i++) {
      GoogleRouteResponseDTO.Leg leg = legs.get(i);
      com.sr.serviceroute.model.RotaLeg rotaLeg = new com.sr.serviceroute.model.RotaLeg();

      rotaLeg.setRota(rota);
      rotaLeg.setDistanceMeters(leg.getDistanceMeters());
      try {
        rotaLeg.setDurationSeconds((int) convertDurationStringToLong(leg.getDuration()));
      } catch (NumberFormatException e) {
        log.warn("Falha ao converter duração para leg {}: {}", i, leg.getDuration());
        rotaLeg.setDurationSeconds(0);
      }

      if (leg.getPolyline() != null) {
        rotaLeg.setEncodedPolyline(leg.getPolyline().getEncodedPolyline());
      }

      if (leg.getStartLocation() != null && leg.getStartLocation().getLatLng() != null) {
        rotaLeg.setStartLat(leg.getStartLocation().getLatLng().getLatitude());
        rotaLeg.setStartLng(leg.getStartLocation().getLatLng().getLongitude());
      }

      if (leg.getEndLocation() != null && leg.getEndLocation().getLatLng() != null) {
        rotaLeg.setEndLat(leg.getEndLocation().getLatLng().getLatitude());
        rotaLeg.setEndLng(leg.getEndLocation().getLatLng().getLongitude());
      }

      rotaLeg.setSeq(i);
      rotaLegs.add(rotaLeg);
    }

    return rotaLegs;
  }
}
