package com.sr.serviceroute.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoutePlanDTO(
    UUID id,
    UUID rotaId,
    UUID instanciaRotaId,
    String status,
    String solver,
    String objective,
    String planJson,
    OffsetDateTime criadoEm,
    OffsetDateTime atualizadoEm,
    OffsetDateTime appliedAt) {
}
