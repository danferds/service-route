package com.sr.serviceroute.dto;

import jakarta.validation.constraints.*;

public record UsuarioCreateDTO(
    @NotBlank String nome,
    @Email @NotBlank String email) {
}
