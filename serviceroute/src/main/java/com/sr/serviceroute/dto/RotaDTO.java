package com.sr.serviceroute.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RotaDTO(
    UUID id,
    UUID ownerId,
    String nome,
    String descricao,
    OffsetDateTime criadoEm,
    OffsetDateTime atualizadoEm) {
}
