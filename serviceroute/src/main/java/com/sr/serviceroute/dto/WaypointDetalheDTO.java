package com.sr.serviceroute.dto;

import java.util.UUID;

public record WaypointDetalheDTO(
    UUID id,
    Double latitude,
    Double longitude,
    String endereco) {
}
