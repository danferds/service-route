package com.sr.serviceroute.dto;

import jakarta.validation.constraints.Size;

public record RotaUpdateDTO(
    @Size(min = 1) String nome,
    String descricao) {
}
