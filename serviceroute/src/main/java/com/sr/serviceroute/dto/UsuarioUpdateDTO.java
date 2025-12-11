package com.sr.serviceroute.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
    @Size(min = 1) String nome,
    @Email String email) {
}
