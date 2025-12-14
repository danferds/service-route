package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.CriarRotaDTO;
import com.sr.serviceroute.model.Cliente;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.model.enums.RotaStatus;
import com.sr.serviceroute.model.enums.RotaWaypointTipo;
import com.sr.serviceroute.repository.ClienteRepository;
import com.sr.serviceroute.repository.RotaRepository;
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

  public RotaService(RotaRepository rotaRepository,
      ClienteRepository clienteRepository,
      WaypointService waypointService,
      RotaWaypointService rotaWaypointService) {
    this.rotaRepository = rotaRepository;
    this.clienteRepository = clienteRepository;
    this.waypointService = waypointService;
    this.rotaWaypointService = rotaWaypointService;
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
}
