package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.*;
import com.sr.serviceroute.model.RoutePlan;
import com.sr.serviceroute.service.RoutePlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rotas/{idRota}/plans")
public class RoutePlanController {

  private final RoutePlanService service;

  public RoutePlanController(RoutePlanService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<?> createPlan(@PathVariable UUID idRota, @RequestBody CreatePlanDTO dto) {
    RoutePlan rp = service.createPlan(dto, idRota);
    URI location = URI.create(String.format("/api/rotas/%s/plans/%s", idRota, rp.getId()));
    return ResponseEntity.created(location).body(Map.of("planId", rp.getId(), "status", rp.getStatus()));
  }

  @GetMapping
  public ResponseEntity<List<RoutePlan>> listPlans(@PathVariable UUID idRota) {
    return ResponseEntity.ok(service.listByRota(idRota));
  }

  @GetMapping("/{planId}")
  public ResponseEntity<RoutePlan> getPlan(@PathVariable UUID idRota, @PathVariable UUID planId) {
    RoutePlan rp = service.getPlan(planId);
    if (rp.getRotaId() != null && !rp.getRotaId().equals(idRota)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(rp);
  }

  @PostMapping("/{planId}/apply")
  public ResponseEntity<Void> applyPlan(@PathVariable UUID idRota, @PathVariable UUID planId) {
    service.applyPlan(planId, idRota);
    return ResponseEntity.ok().build();
  }
}
