package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.RegistrarVisitaDTO;
import com.sr.serviceroute.dto.VisitaResponseDTO;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.model.Viagem;
import com.sr.serviceroute.model.Visitas;
import com.sr.serviceroute.repository.RotaWaypointRepository;
import com.sr.serviceroute.repository.ViagemRepository;
import com.sr.serviceroute.repository.VisitasRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VisitasService {

  private final VisitasRepository visitasRepository;
  private final ViagemRepository viagemRepository;
  private final RotaWaypointRepository rotaWaypointRepository;

  public VisitaResponseDTO registrar(UUID viagemId, RegistrarVisitaDTO dto) {
    Viagem viagem = viagemRepository.findById(viagemId)
        .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));

    validarViagemEmAndamento(viagem);

    RotaWaypoint rotaWaypoint = rotaWaypointRepository.findById(dto.rotaWaypointId())
        .orElseThrow(() -> new EntityNotFoundException("Waypoint da rota não encontrado"));

    if (!rotaWaypoint.getRota().getId().equals(viagem.getRota().getId())) {
      throw new IllegalArgumentException("Waypoint não pertence à rota da viagem");
    }

    boolean visitaJaRegistrada = visitasRepository.existsByViagemIdAndRotaWaypointId(
        viagem.getId(),
        rotaWaypoint.getId());
    if (visitaJaRegistrada) {
      throw new IllegalStateException("Visita já registrada para este waypoint e viagem");
    }

    Visitas visita = visitasRepository.save(
        Visitas.builder()
            .viagem(viagem)
            .rotaWaypoint(rotaWaypoint)
            .etaReal(dto.etaReal())
            .build());

    return VisitaResponseDTO.fromEntity(visita);
  }

  @Transactional(readOnly = true)
  public List<VisitaResponseDTO> listarPorViagem(UUID viagemId) {
    if (!viagemRepository.existsById(viagemId)) {
      throw new EntityNotFoundException("Viagem não encontrada");
    }

    return visitasRepository.findByViagemIdOrderByDataCriacaoAsc(viagemId).stream()
        .map(VisitaResponseDTO::fromEntity)
        .toList();
  }

  @Transactional(readOnly = true)
  public VisitaResponseDTO buscar(UUID visitaId) {
    Visitas visita = visitasRepository.findById(visitaId)
        .orElseThrow(() -> new EntityNotFoundException("Visita não encontrada"));
    return VisitaResponseDTO.fromEntity(visita);
  }

  private void validarViagemEmAndamento(Viagem viagem) {
    if (viagem.getDataInicio() == null) {
      throw new IllegalStateException("Viagem ainda não foi iniciada");
    }
    if (viagem.getDataFim() != null) {
      throw new IllegalStateException("Viagem já foi finalizada");
    }
  }
}
