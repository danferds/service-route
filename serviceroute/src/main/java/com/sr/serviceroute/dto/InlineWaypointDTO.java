package com.sr.serviceroute.dto;

import java.time.OffsetDateTime;

public record InlineWaypointDTO(
    String tempId,
    String nome,
    Double latitude,
    Double longitude,
    OffsetDateTime eta) {
}
