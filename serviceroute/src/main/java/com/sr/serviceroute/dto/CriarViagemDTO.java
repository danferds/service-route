package com.sr.serviceroute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarViagemDTO(
    @NotNull UUID rotaId,

    @NotBlank String veiculoRef) {
}
