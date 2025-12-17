package com.sr.serviceroute.dto;

import com.sr.serviceroute.model.enums.RotaStatus;

import java.util.List;
import java.util.UUID;

public record RotaCompletaDTO(
    UUID id,
    String nome,
    RotaStatus status,
    Integer tempoEstimadoTotal,
    List<RotaWaypointDetalheDTO> waypoints) {
}
