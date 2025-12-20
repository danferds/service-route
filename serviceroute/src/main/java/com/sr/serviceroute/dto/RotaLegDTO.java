package com.sr.serviceroute.dto;

import java.util.UUID;

public record RotaLegDTO(
    UUID id,
    Long distanceMeters,
    Integer durationSeconds,
    Integer seq,
    Double startLat,
    Double startLng,
    Double endLat,
    Double endLng,
    String encodedPolyline) {
}
