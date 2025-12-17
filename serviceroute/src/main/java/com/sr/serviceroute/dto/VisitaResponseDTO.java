package com.sr.serviceroute.dto;

import com.sr.serviceroute.model.Visitas;

import java.time.Instant;
import java.util.UUID;

public record VisitaResponseDTO(
    UUID id,
    UUID viagemId,
    UUID rotaWaypointId,
    Instant etaPrevisto,
    Instant etaReal) {

  public static VisitaResponseDTO fromEntity(Visitas visita) {
    return new VisitaResponseDTO(
        visita.getId(),
        visita.getViagem().getId(),
        visita.getRotaWaypoint().getId(),
        visita.getRotaWaypoint().getEtaPrevisto(),
        visita.getEtaReal());
  }
}
