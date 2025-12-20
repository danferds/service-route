package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.CriarRotaDTO;
import com.sr.serviceroute.model.Cliente;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.model.enums.RotaStatus;
import com.sr.serviceroute.model.enums.RotaWaypointTipo;
import com.sr.serviceroute.repository.ClienteRepository;
import com.sr.serviceroute.repository.RotaRepository;
import com.sr.serviceroute.service.planning.RotaPlanningService;
import com.sr.serviceroute.dto.RotaDetalhadaDTO;
import com.sr.serviceroute.dto.RotaWaypointDetalheDTO;
import com.sr.serviceroute.dto.RotaLegDTO;
import com.sr.serviceroute.dto.WaypointDetalheDTO;
import com.sr.serviceroute.model.RotaWaypoint;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RotaService {
  private final RotaRepository rotaRepository;
  private final ClienteRepository clienteRepository;
  private final WaypointService waypointService;
  private final RotaWaypointService rotaWaypointService;
  private final RotaPlanningService rotaPlanningService;

  public RotaService(RotaRepository rotaRepository,
      ClienteRepository clienteRepository,
      WaypointService waypointService,
      RotaWaypointService rotaWaypointService,
      RotaPlanningService rotaPlanningService) {
    this.rotaRepository = rotaRepository;
    this.clienteRepository = clienteRepository;
    this.waypointService = waypointService;
    this.rotaWaypointService = rotaWaypointService;
    this.rotaPlanningService = rotaPlanningService;
  }

  @Transactional
  public UUID criarRota(CriarRotaDTO dto) {

    // 1. Validar cliente
    Cliente cliente = clienteRepository.findById(dto.clienteId())
        .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

    // 2. Criar rota
    Rota rota = rotaRepository.save(
        Rota.builder()
            .nome(dto.nome())
            .cliente(cliente)
            .status(RotaStatus.CRIADA)
            .build());

    int sequencia = 1;

    Waypoint origem = waypointService.criar(dto.origem());
    rotaWaypointService.vincular(
        rota,
        origem,
        RotaWaypointTipo.ORIGEM,
        sequencia++);

    List<?> paradas = dto.paradas();
    if (paradas != null && !paradas.isEmpty()) {
      for (var paradaDto : dto.paradas()) {
        Waypoint parada = waypointService.criar(paradaDto);
        rotaWaypointService.vincular(
            rota,
            parada,
            RotaWaypointTipo.PARADA,
            null);
        sequencia++;
      }
    }

    Waypoint destino = waypointService.criar(dto.destino());
    rotaWaypointService.vincular(
        rota,
        destino,
        RotaWaypointTipo.DESTINO,
        sequencia);

    return rota.getId();
  }

  @Transactional
  public void planejarRota(UUID rotaId) {

    Rota rota = rotaRepository.findById(rotaId)
        .orElseThrow(() -> new IllegalArgumentException("Rota não encontrada"));

    if (rota.getStatus() != RotaStatus.CRIADA) {
      throw new IllegalStateException(
          "Rota não pode ser planejada no estado atual");
    }

    try {
      rota.setStatus(RotaStatus.PLANEJANDO);

      rotaPlanningService.planejar(rotaId);

      rota.setStatus(RotaStatus.PLANEJADA);

    } catch (Exception ex) {
      rota.setStatus(RotaStatus.ERRO);
      // futuramente: persistir mensagem de erro
      throw ex;
    }
  }

  public RotaDetalhadaDTO obterDetalhesRota(UUID rotaId) {
    Rota rota = rotaRepository.findById(rotaId)
        .orElseThrow(() -> new EntityNotFoundException("Rota não encontrada"));

    List<RotaWaypoint> waypoints = rotaWaypointService.listarPorRota(rotaId);

    RotaWaypointDetalheDTO origem = null;
    RotaWaypointDetalheDTO destino = null;
    List<RotaWaypointDetalheDTO> paradas = new java.util.ArrayList<>();

    for (RotaWaypoint rw : waypoints) {
      RotaWaypointDetalheDTO dto = new RotaWaypointDetalheDTO(
          rw.getId(),
          rw.getTipo(),
          rw.getSeq(),
          rw.getEtaPrevisto(),
          new WaypointDetalheDTO(
              rw.getWaypoint().getId(),
              rw.getWaypoint().getLatitude(),
              rw.getWaypoint().getLongitude(),
              rw.getWaypoint().getEndereco()));

      if (rw.getTipo() == RotaWaypointTipo.ORIGEM) {
        origem = dto;
      } else if (rw.getTipo() == RotaWaypointTipo.DESTINO) {
        destino = dto;
      } else {
        paradas.add(dto);
      }
    }

    List<RotaLegDTO> legs = new java.util.ArrayList<>();
    if (rota.getLegs() != null) {
      legs = rota.getLegs().stream().map(leg -> new RotaLegDTO(
          leg.getId(),
          leg.getDistanceMeters(),
          leg.getDurationSeconds(),
          leg.getSeq(),
          leg.getStartLat(),
          leg.getStartLng(),
          leg.getEndLat(),
          leg.getEndLng(),
          leg.getEncodedPolyline())).toList();
    }

    return new RotaDetalhadaDTO(
        rota.getId(),
        rota.getNome(),
        rota.getStatus(),
        rota.getTempoEstimadoTotal(),
        rota.getDataCriacao(),
        rota.getCliente().getId(),
        origem,
        destino,
        paradas,
        legs);
  }
}
