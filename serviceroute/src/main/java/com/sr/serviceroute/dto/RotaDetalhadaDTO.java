package com.sr.serviceroute.dto;

import com.sr.serviceroute.model.enums.RotaStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RotaDetalhadaDTO(
    UUID id,
    String nome,
    RotaStatus status,
    Integer tempoEstimadoTotal,
    Instant dataCriacao,
    UUID clienteId,
    RotaWaypointDetalheDTO origem,
    RotaWaypointDetalheDTO destino,
    List<RotaWaypointDetalheDTO> paradas,
    List<RotaLegDTO> legs) {
}
