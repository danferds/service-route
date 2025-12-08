package com.sr.serviceroute.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WaypointDTO(
    UUID id,
    Integer seq,
    String nome,
    Double latitude,
    Double longitude,
    OffsetDateTime eta) {
}
