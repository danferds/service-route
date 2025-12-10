package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.*;
import com.sr.serviceroute.model.*;
import com.sr.serviceroute.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Service
public class RoutePlanService {

  private final RoutePlanRepository routePlanRepo;
  private final PlanWaypointRepository planWpRepo;
  private final WaypointRepository waypointRepo;
  private final EntityManager em;

  public RoutePlanService(RoutePlanRepository routePlanRepo,
      PlanWaypointRepository planWpRepo,
      WaypointRepository waypointRepo,
      EntityManager em) {
    this.routePlanRepo = routePlanRepo;
    this.planWpRepo = planWpRepo;
    this.waypointRepo = waypointRepo;
    this.em = em;
  }

  @Transactional
  public RoutePlan createPlan(CreatePlanDTO dto, UUID rotaId) {
    RoutePlan rp = RoutePlan.builder()
        .id(UUID.randomUUID())
        .rotaId(rotaId)
        .instanciaRotaId(dto.instanciaRotaId())
        .status("pending")
        .solver(null)
        .objective(dto.objective())
        .params(dto.paramsJson())
        .planJson(null)
        .criadoEm(OffsetDateTime.now())
        .atualizadoEm(OffsetDateTime.now())
        .build();

    routePlanRepo.save(rp);

    int seq = 1;
    if (dto.waypointIds() != null) {
      for (String wid : dto.waypointIds()) {
        PlanWaypoint pw = PlanWaypoint.builder()
            .id(UUID.randomUUID())
            .routePlanId(rp.getId())
            .seq(seq++)
            .ref(wid)
            .type("existing")
            .createdAt(OffsetDateTime.now())
            .build();
        planWpRepo.save(pw);
      }
    }
    if (dto.inlineWaypoints() != null) {
      for (InlineWaypointDTO iw : dto.inlineWaypoints()) {
        PlanWaypoint pw = PlanWaypoint.builder()
            .id(UUID.randomUUID())
            .routePlanId(rp.getId())
            .seq(seq++)
            .ref(iw.tempId())
            .type("inline")
            .nome(iw.nome())
            .latitude(iw.latitude())
            .longitude(iw.longitude())
            .eta(iw.eta())
            .createdAt(OffsetDateTime.now())
            .build();
        planWpRepo.save(pw);
      }
    }

    return rp;
  }

  public RoutePlan getPlan(UUID planId) {
    return routePlanRepo.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
  }

  public List<RoutePlan> listByRota(UUID rotaId) {
    return routePlanRepo.findByRotaId(rotaId);
  }

  @Transactional
  public void applyPlan(UUID planId, UUID rotaId) {
    RoutePlan rp = getPlan(planId);

    if ("applied".equals(rp.getStatus()) || rp.getAppliedAt() != null)
      return;

    List<com.sr.serviceroute.model.Waypoint> locked = em.createQuery(
        "SELECT w FROM Waypoint w WHERE w.rotaId = :rotaId ORDER BY w.seq", com.sr.serviceroute.model.Waypoint.class)
        .setParameter("rotaId", rotaId)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .getResultList();

    int maxSeq = locked.stream().map(com.sr.serviceroute.model.Waypoint::getSeq)
        .max(Integer::compareTo).orElse(0);

    List<PlanWaypoint> planWps = this.planWpRepo.findByRoutePlanIdOrderBySeqAsc(planId);

    Map<String, UUID> mapping = new HashMap<>();

    for (PlanWaypoint pw : planWps) {
      if ("existing".equals(pw.getType())) {
        try {
          UUID existingId = UUID.fromString(pw.getRef());
          Optional<com.sr.serviceroute.model.Waypoint> maybe = waypointRepo.findById(existingId);
          if (maybe.isPresent()) {
            // validate membership
            if (!Objects.equals(maybe.get().getRotaId(), rotaId)) {
              throw new BadRequestException("Waypoint " + existingId + " does not belong to rota " + rotaId);
            }
            mapping.put(pw.getRef(), existingId);
            continue;
          } else {
            throw new BadRequestException("Referenced waypoint not found: " + pw.getRef());
          }
        } catch (IllegalArgumentException ex) {
          throw new BadRequestException("Invalid waypoint id format: " + pw.getRef());
        }
      } else if ("inline".equals(pw.getType())) {
        maxSeq += 1;
        com.sr.serviceroute.model.Waypoint newWp = com.sr.serviceroute.model.Waypoint.builder()
            .id(UUID.randomUUID())
            .rotaId(rotaId)
            .seq(maxSeq)
            .nome(pw.getNome())
            .latitude(pw.getLatitude())
            .longitude(pw.getLongitude())
            .eta(pw.getEta())
            .criadoEm(OffsetDateTime.now())
            .build();
        waypointRepo.save(newWp);
        mapping.put(pw.getRef(), newWp.getId());
      } else {
        throw new BadRequestException("Unknown plan waypoint type: " + pw.getType());
      }
    }

    rp.setStatus("applied");
    rp.setAppliedAt(OffsetDateTime.now());
    rp.setAtualizadoEm(OffsetDateTime.now());
    routePlanRepo.save(rp);
  }
}
