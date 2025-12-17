package com.sr.serviceroute.service;

import com.sr.serviceroute.model.Visitas;
import com.sr.serviceroute.model.enums.AlertaTipo;
import com.sr.serviceroute.repository.VisitasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitasMonitorService {

  private final VisitasRepository visitasRepository;
  private final AlertaService alertaService;

  @Value("${alerts.monitor.atraso-minutos:1}")
  private long atrasoMinimoMinutos;

  @Transactional
  public void processarAtrasosPendentes() {
    List<Visitas> visitas = visitasRepository.buscarVisitasElegiveisParaAlerta(AlertaTipo.ATRASO);
    if (visitas.isEmpty()) {
      log.debug("Nenhuma visita elegível para alerta de atraso");
      return;
    }

    Duration atrasoMinimo = Duration.ofMinutes(atrasoMinimoMinutos);

    for (Visitas visita : visitas) {
      Duration atraso = calcularAtraso(visita);
      if (atraso.isNegative() || atraso.compareTo(atrasoMinimo) < 0) {
        continue;
      }
      alertaService.registrarAtraso(visita, atraso);
    }
  }

  private Duration calcularAtraso(Visitas visita) {
    Instant previsto = visita.getRotaWaypoint().getEtaPrevisto();
    Instant real = visita.getEtaReal();

    if (previsto == null || real == null) {
      return Duration.ZERO;
    }

    return Duration.between(previsto, real);
  }
}
