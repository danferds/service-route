package com.sr.serviceroute.service;

import com.sr.serviceroute.model.Alertas;
import com.sr.serviceroute.model.Visitas;
import com.sr.serviceroute.model.enums.AlertaTipo;
import com.sr.serviceroute.repository.AlertasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertaService {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

  private final AlertasRepository alertasRepository;

  public void registrarAtraso(Visitas visita, Duration atraso) {
    if (alertasRepository.existsByVisitaIdAndTipo(visita.getId(), AlertaTipo.ATRASO)) {
      return;
    }

    String texto = construirMensagem(visita, atraso);

    Alertas alerta = Alertas.builder()
        .visita(visita)
        .tipo(AlertaTipo.ATRASO)
        .texto(texto)
        .build();

    alertasRepository.save(alerta);
  }

  private String construirMensagem(Visitas visita, Duration atraso) {
    var rotaWaypoint = visita.getRotaWaypoint();
    long minutos = atraso.toMinutes();
    String previsto = rotaWaypoint.getEtaPrevisto() != null
        ? FORMATTER.format(rotaWaypoint.getEtaPrevisto())
        : "-";
    String real = visita.getEtaReal() != null
        ? FORMATTER.format(visita.getEtaReal())
        : "-";
    return String.format(
        "Visita ao waypoint %s (seq %s) atrasou %d minutos. Previsto: %s | Real: %s",
        rotaWaypoint.getTipo(),
        rotaWaypoint.getSeq(),
        minutos,
        previsto,
        real);
  }
}
