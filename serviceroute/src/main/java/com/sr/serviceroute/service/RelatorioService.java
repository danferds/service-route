package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.RelatorioViagemDTO;
import com.sr.serviceroute.model.Viagem;
import com.sr.serviceroute.model.Visitas;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.repository.ViagemRepository;
import com.sr.serviceroute.repository.VisitasRepository;
import com.sr.serviceroute.repository.RotaWaypointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelatorioService {

  private final ViagemRepository viagemRepository;
  private final VisitasRepository visitasRepository;
  private final RotaWaypointRepository rotaWaypointRepository;

  @Transactional(readOnly = true)
  public List<RelatorioViagemDTO> gerarRelatorioViagensEmAndamento() {
    List<Viagem> viagensEmAndamento = viagemRepository.findByDataInicioIsNotNullAndDataFimIsNull();

    return viagensEmAndamento.stream()
        .map(this::montarRelatorio)
        .toList();
  }

  private RelatorioViagemDTO montarRelatorio(Viagem viagem) {
    List<Visitas> visitas = visitasRepository.findByViagemIdOrderByDataCriacaoAsc(viagem.getId());

    Optional<Visitas> ultimaVisita = visitas.stream()
        .max(Comparator.comparing(Visitas::getDataCriacao));

    String ultimoPonto = ultimaVisita
        .map(v -> v.getRotaWaypoint().getWaypoint().getEndereco()) // Ou outro identificador mais amigável
        .orElse("Início da Rota");

    RotaWaypoint proximoWaypoint = calcularProximoWaypoint(viagem, ultimaVisita);
    String proximoPonto = proximoWaypoint != null
        ? proximoWaypoint.getWaypoint().getEndereco()
        : "Destino Final";

    Duration atraso = calcularAtraso(viagem, ultimaVisita, proximoWaypoint);

    // Se o atraso for negativo (adiantado) ou muito pequeno, consideramos zero para
    // relatorio?
    // Vamos manter o valor real, se positivo é atraso.
    long atrasoMinutos = atraso.toMinutes();
    String status = atrasoMinutos > 5 ? "ATRASADO" : "NO_HORARIO"; // Tolerância de 5 min

    String localizacaoEstimada = estimarLocalizacao(ultimaVisita, proximoWaypoint, atraso);

    return new RelatorioViagemDTO(
        viagem.getId(),
        viagem.getRota().getId(),
        viagem.getRota().getNome(),
        viagem.getVeiculoRef(),
        status,
        ultimoPonto,
        proximoPonto,
        atrasoMinutos > 0 ? atrasoMinutos : 0,
        localizacaoEstimada);
  }

  private RotaWaypoint calcularProximoWaypoint(Viagem viagem, Optional<Visitas> ultimaVisita) {
    if (ultimaVisita.isEmpty()) {
      return rotaWaypointRepository.findByRotaIdOrderBySeqAsc(viagem.getRota().getId()).stream()
          .findFirst()
          .orElse(null);
    }

    Visitas ultima = ultimaVisita.get();
    Integer seqAtual = ultima.getRotaWaypoint().getSeq();

    return rotaWaypointRepository.findByRotaIdOrderBySeqAsc(viagem.getRota().getId()).stream()
        .filter(rw -> rw.getSeq() > seqAtual)
        .findFirst()
        .orElse(null);
  }

  private Duration calcularAtraso(Viagem viagem, Optional<Visitas> ultimaVisita, RotaWaypoint proximoWaypoint) {
    // Se temos visitas, o atraso é baseadono ETA Real vs Previsto da última visita
    if (ultimaVisita.isPresent()) {
      Visitas v = ultimaVisita.get();
      if (v.getEtaReal() != null && v.getRotaWaypoint().getEtaPrevisto() != null) {
        return Duration.between(v.getRotaWaypoint().getEtaPrevisto(), v.getEtaReal());
      }
    }

    // Se não tem visitas, verificamos se já deveria ter chegado no primeiro ponto
    // ou iniciado
    // Considerando o start da viagem vs agora, e o primeiro ponto.
    // Simplificação: Se não tem visitas, comparamos NOW com o ETA do próximo
    // (primeiro) ponto?
    // Se NOW > ETA_Proximo, então está muito atrasado.

    if (proximoWaypoint != null && proximoWaypoint.getEtaPrevisto() != null) {
      Instant now = Instant.now();
      if (now.isAfter(proximoWaypoint.getEtaPrevisto())) {
        return Duration.between(proximoWaypoint.getEtaPrevisto(), now);
      }
    }

    return Duration.ZERO;
  }

  private String estimarLocalizacao(Optional<Visitas> ultimaVisita, RotaWaypoint proximoWaypoint, Duration atraso) {
    if (ultimaVisita.isPresent()) {
      long minutosDesdeVisita = Duration.between(ultimaVisita.get().getDataCriacao(), Instant.now()).toMinutes();
      return "Passou por " + ultimaVisita.get().getRotaWaypoint().getWaypoint().getEndereco() + " há "
          + minutosDesdeVisita + " minutos.";
    } else {
      return "Em trânsito para o primeiro ponto.";
    }
  }
}
