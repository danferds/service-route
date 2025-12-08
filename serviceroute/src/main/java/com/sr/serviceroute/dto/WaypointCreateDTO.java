package com.sr.serviceroute.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record WaypointCreateDTO(
    @Min(1) Integer seq,
    @Size(max = 255) String nome,
    @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
    @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude,
    OffsetDateTime eta) {
}
