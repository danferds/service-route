package com.sr.serviceroute.dto;

import com.sr.serviceroute.model.Viagem;

import java.time.Instant;
import java.util.UUID;

public record ViagemResponseDTO(
    UUID id,
    UUID rotaId,
    String rotaNome,
    String veiculoRef,
    Instant dataInicio,
    Instant dataFim,
    Integer tempoRealTotal) {

  public static ViagemResponseDTO fromEntity(Viagem viagem) {
    return new ViagemResponseDTO(
        viagem.getId(),
        viagem.getRota().getId(),
        viagem.getRota().getNome(),
        viagem.getVeiculoRef(),
        viagem.getDataInicio(),
        viagem.getDataFim(),
        viagem.getTempoRealTotal());
  }
}
