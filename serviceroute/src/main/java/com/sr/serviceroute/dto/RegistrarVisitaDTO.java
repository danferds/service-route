package com.sr.serviceroute.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record RegistrarVisitaDTO(
    @NotNull UUID rotaWaypointId,

    @NotNull Instant etaReal) {
}
