package com.sr.serviceroute.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UsuarioDTO(
    UUID id,
    String nome,
    String email,
    OffsetDateTime criadoEm) {
}
