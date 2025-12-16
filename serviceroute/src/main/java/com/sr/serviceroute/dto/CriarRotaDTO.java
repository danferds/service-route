package com.sr.serviceroute.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CriarRotaDTO(
    @NotNull UUID clienteId,

    @NotBlank String nome,

    @Valid @NotNull WaypointCreateDTO origem,

    @Valid @NotNull WaypointCreateDTO destino,

    @Valid List<WaypointCreateDTO> paradas) {

}
