package com.sr.serviceroute.dto;

import com.sr.serviceroute.model.enums.RotaWaypointTipo;

import java.time.Instant;
import java.util.UUID;

public record RotaWaypointDetalheDTO(
    UUID id,
    RotaWaypointTipo tipo,
    Integer seq,
    Instant etaPrevisto,
    WaypointDetalheDTO waypoint) {
}
