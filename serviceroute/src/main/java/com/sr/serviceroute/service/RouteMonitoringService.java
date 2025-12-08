package com.sr.serviceroute.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sr.serviceroute.model.Alerta;
import com.sr.serviceroute.model.EventoRastreamento;
import com.sr.serviceroute.model.InstanciaRota;
import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.model.WaypointVisit;
import com.sr.serviceroute.repository.AlertaRepository;
import com.sr.serviceroute.repository.EventoRastreamentoRepository;
import com.sr.serviceroute.repository.InstanciaRotaRepository;
import com.sr.serviceroute.repository.WaypointRepository;
import com.sr.serviceroute.repository.WaypointVisitRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteMonitoringService {
  private static final Logger log = LoggerFactory.getLogger(RouteMonitoringService.class);

  private final EventoRastreamentoRepository eventoRepo;
  private final WaypointRepository waypointRepo;
  private final InstanciaRotaRepository instanciaRepo;
  private final WaypointVisitRepository visitRepo;
  private final AlertaRepository alertaRepo;

  private final long pollingIntervalMs;
  private final double radiusMeters;
  private final long lateThresholdMin;

  public RouteMonitoringService(EventoRastreamentoRepository eventoRepo,
      WaypointRepository waypointRepo,
      InstanciaRotaRepository instanciaRepo,
      WaypointVisitRepository visitRepo,
      AlertaRepository alertaRepo,
      @Value("${app.worker.polling-interval-ms}") long pollingIntervalMs,
      @Value("${app.worker.radius-meters}") double radiusMeters,
      @Value("${app.worker.late-threshold-min}") long lateThresholdMin) {
    this.eventoRepo = eventoRepo;
    this.waypointRepo = waypointRepo;
    this.instanciaRepo = instanciaRepo;
    this.visitRepo = visitRepo;
    this.alertaRepo = alertaRepo;
    this.pollingIntervalMs = pollingIntervalMs;
    this.radiusMeters = radiusMeters;
    this.lateThresholdMin = lateThresholdMin;
  }

  @Scheduled(fixedDelayString = "${app.worker.polling-interval-ms}")
  public void pollAndProcess() {
    try {
      OffsetDateTime since = OffsetDateTime.now().minusSeconds(Math.max(60, pollingIntervalMs / 1000 * 3));
      List<java.util.UUID> activeInstancias = eventoRepo.findActiveInstancias(since);

      if (activeInstancias.isEmpty()) {
        log.debug("No active instances found since {}", since);
        return;
      }

      for (java.util.UUID instanciaId : activeInstancias) {
        try {
          processInstancia(instanciaId);
        } catch (Exception ex) {
          log.error("Erro ao processar instancia {}: {}", instanciaId, ex.getMessage(), ex);
        }
      }
    } catch (Exception e) {
      log.error("Erro no pollAndProcess: {}", e.getMessage(), e);
    }
  }

  @Transactional
  protected void processInstancia(java.util.UUID instanciaId) {
    OffsetDateTime since = OffsetDateTime.now().minusMinutes(30);
    List<EventoRastreamento> events = eventoRepo.findRecentByInstancia(instanciaId, since);
    if (events.isEmpty())
      return;

    Optional<InstanciaRota> optInst = instanciaRepo.findById(instanciaId);
    if (optInst.isEmpty())
      return;
    InstanciaRota instancia = optInst.get();

    List<Waypoint> waypoints = waypointRepo.findByRotaOrderBySeq(instancia.getRotaId());
    if (waypoints.isEmpty())
      return;

    Set<java.util.UUID> visitedWaypointIds = visitRepo.findAll().stream()
        .filter(v -> v.getInstanciaRotaId().equals(instanciaId))
        .map(WaypointVisit::getWaypointId)
        .collect(Collectors.toSet());

    List<Waypoint> pending = waypoints.stream()
        .filter(wp -> !visitedWaypointIds.contains(wp.getId()))
        .collect(Collectors.toList());

    if (pending.isEmpty())
      return;

    int checkCount = Math.min(3, pending.size());
    List<Waypoint> toCheck = pending.subList(0, checkCount);

    for (EventoRastreamento ev : events) {
      for (Waypoint wp : toCheck) {
        double dist = haversineMeters(ev.getLatitude(), ev.getLongitude(), wp.getLatitude(), wp.getLongitude());
        if (dist <= radiusMeters) {
          WaypointVisit visit = WaypointVisit.builder()
              .id(UUID.randomUUID())
              .instanciaRotaId(instanciaId)
              .waypointId(wp.getId())
              .chegouEm(ev.getGravadoEm())
              .criadoPor(ev.getDispositivoId())
              .distanciaMetros(dist)
              .criadoEm(OffsetDateTime.now())
              .build();

          try {
            visitRepo.save(visit);
            log.info("Waypoint {} visitado para instancia {} (dist={}m)", wp.getId(), instanciaId, dist);
          } catch (DataIntegrityViolationException die) {
            log.debug("Waypoint visita duplicada (instancia={}, waypoint={})", instanciaId, wp.getId());
          }

          // create alert if late
          if (wp.getEta() != null) {
            OffsetDateTime arrived = ev.getGravadoEm();
            OffsetDateTime lateThreshold = wp.getEta().plusMinutes(lateThresholdMin);
            if (arrived.isAfter(lateThreshold)) {
              Alerta alerta = Alerta.builder()
                  .id(UUID.randomUUID())
                  .instanciaRotaId(instanciaId)
                  .waypointId(wp.getId())
                  .tipo("delay")
                  .message(String.format("Atraso de %d minutos",
                      java.time.Duration.between(wp.getEta(), arrived).toMinutes()))
                  .criadoEm(OffsetDateTime.now())
                  .resolvido(false)
                  .build();
              alertaRepo.save(alerta);
              log.info("Alerta de atraso criado para a instancia {} waypoint {}", instanciaId, wp.getId());
            }
          }

          instancia.setAtualWaypointId(wp.getId());
          instanciaRepo.save(instancia);

          break;
        }
      }
    }
  }

  private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371000;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }
}
