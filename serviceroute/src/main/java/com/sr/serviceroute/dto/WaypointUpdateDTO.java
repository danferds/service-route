package com.sr.serviceroute.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record WaypointUpdateDTO(
    @Min(1) Integer seq,
    @Size(max = 255) String nome,
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude,
    OffsetDateTime eta) {
}
