package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.CriarViagemDTO;
import com.sr.serviceroute.dto.FinalizarViagemDTO;
import com.sr.serviceroute.dto.IniciarViagemDTO;
import com.sr.serviceroute.dto.RotaCompletaDTO;
import com.sr.serviceroute.dto.RotaWaypointDetalheDTO;
import com.sr.serviceroute.dto.ViagemResponseDTO;
import com.sr.serviceroute.dto.WaypointDetalheDTO;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.model.Viagem;
import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.model.enums.RotaStatus;
import com.sr.serviceroute.repository.RotaRepository;
import com.sr.serviceroute.repository.RotaWaypointRepository;
import com.sr.serviceroute.repository.ViagemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ViagemService {

  private final ViagemRepository viagemRepository;
  private final RotaRepository rotaRepository;
  private final RotaWaypointRepository rotaWaypointRepository;

  public ViagemResponseDTO criar(CriarViagemDTO dto) {
    Rota rota = rotaRepository.findById(dto.rotaId())
        .orElseThrow(() -> new EntityNotFoundException("Rota não encontrada"));

    if (rota.getStatus() != RotaStatus.PLANEJADA) {
      throw new IllegalStateException("A rota precisa estar planejada para gerar viagens");
    }

    Viagem viagem = viagemRepository.save(
        Viagem.builder()
            .rota(rota)
            .veiculoRef(dto.veiculoRef())
            .build());

    return ViagemResponseDTO.fromEntity(viagem);
  }

  public ViagemResponseDTO iniciar(UUID viagemId, IniciarViagemDTO dto) {
    Viagem viagem = buscarEntidade(viagemId);

    if (viagem.getDataInicio() != null) {
      throw new IllegalStateException("Viagem já foi iniciada");
    }

    Instant inicio = dto != null && dto.dataInicio() != null
        ? dto.dataInicio()
        : Instant.now();
    viagem.setDataInicio(inicio);

    return ViagemResponseDTO.fromEntity(viagem);
  }

  public ViagemResponseDTO finalizar(UUID viagemId, FinalizarViagemDTO dto) {
    Viagem viagem = buscarEntidade(viagemId);

    if (viagem.getDataInicio() == null) {
      throw new IllegalStateException("Viagem ainda não foi iniciada");
    }
    if (viagem.getDataFim() != null) {
      throw new IllegalStateException("Viagem já foi finalizada");
    }

    Instant fim = dto != null && dto.dataFim() != null
        ? dto.dataFim()
        : Instant.now();

    if (fim.isBefore(viagem.getDataInicio())) {
      throw new IllegalArgumentException("Data de término não pode ser anterior ao início");
    }

    viagem.setDataFim(fim);

    Integer tempoCalculado = dto != null ? dto.tempoRealTotal() : null;
    if (tempoCalculado == null) {
      long minutos = Duration.between(viagem.getDataInicio(), fim).toMinutes();
      tempoCalculado = Math.toIntExact(minutos);
    }
    if (tempoCalculado < 0) {
      throw new IllegalArgumentException("Tempo real total inválido");
    }

    viagem.setTempoRealTotal(tempoCalculado);

    return ViagemResponseDTO.fromEntity(viagem);
  }

  @Transactional(readOnly = true)
  public ViagemResponseDTO buscar(UUID viagemId) {
    return ViagemResponseDTO.fromEntity(buscarEntidade(viagemId));
  }

  @Transactional(readOnly = true)
  public List<ViagemResponseDTO> listarPorRota(UUID rotaId) {
    return viagemRepository.findByRotaId(rotaId).stream()
        .map(ViagemResponseDTO::fromEntity)
        .toList();
  }

  @Transactional(readOnly = true)
  public RotaCompletaDTO rotaCompletaDaViagem(UUID viagemId) {
    Viagem viagem = buscarEntidade(viagemId);
    Rota rota = viagem.getRota();

    List<RotaWaypointDetalheDTO> waypoints = rotaWaypointRepository.findByRotaIdOrderBySeqAsc(rota.getId()).stream()
        .map(this::mapWaypoint)
        .toList();

    return new RotaCompletaDTO(
        rota.getId(),
        rota.getNome(),
        rota.getStatus(),
        rota.getTempoEstimadoTotal(),
        waypoints);
  }

  private RotaWaypointDetalheDTO mapWaypoint(RotaWaypoint rotaWaypoint) {
    Waypoint waypoint = rotaWaypoint.getWaypoint();
    WaypointDetalheDTO waypointDetalhe = new WaypointDetalheDTO(
        waypoint.getId(),
        waypoint.getLatitude(),
        waypoint.getLongitude(),
        waypoint.getEndereco());

    return new RotaWaypointDetalheDTO(
        rotaWaypoint.getId(),
        rotaWaypoint.getTipo(),
        rotaWaypoint.getSeq(),
        rotaWaypoint.getEtaPrevisto(),
        waypointDetalhe);
  }

  private Viagem buscarEntidade(UUID viagemId) {
    return viagemRepository.findById(viagemId)
        .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
  }
}
