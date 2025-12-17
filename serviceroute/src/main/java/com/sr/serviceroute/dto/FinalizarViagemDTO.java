package com.sr.serviceroute.dto;

import java.time.Instant;

public record FinalizarViagemDTO(
    Instant dataFim,
    Integer tempoRealTotal) {
}
