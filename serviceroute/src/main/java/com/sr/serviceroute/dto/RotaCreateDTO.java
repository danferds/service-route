package com.sr.serviceroute.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RotaCreateDTO(
    @NotBlank String nome,
    String descricao,
    UUID ownerId) {
}
