package com.sr.serviceroute.dto;

import java.util.UUID;

public record RelatorioViagemDTO(
    UUID viagemId,
    UUID rotaId,
    String nomeRota,
    String veiculo,
    String status, // "NO_HORARIO", "ATRASADO"
    String ultimoPontoVisitado,
    String proximoPonto,
    Long atrasoEmMinutos,
    String localizacaoEstimada) {
}
