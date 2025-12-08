package com.sr.serviceroute.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sr.serviceroute.dto.ReorderDTO;
import com.sr.serviceroute.dto.WaypointCreateDTO;
import com.sr.serviceroute.dto.WaypointDTO;
import com.sr.serviceroute.dto.WaypointUpdateDTO;
import com.sr.serviceroute.service.WaypointService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rotas/{idRota}/waypoints")
@Validated
public class WaypointController {

  private final WaypointService service;

  public WaypointController(WaypointService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<WaypointDTO> create(@PathVariable UUID idRota,
      @RequestBody @Validated WaypointCreateDTO dto) {
    WaypointDTO created = service.create(idRota, dto);
    URI location = URI.create(String.format("/api/rotas/%s/waypoints/%s", idRota, created.id()));
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping
  public ResponseEntity<List<WaypointDTO>> list(@PathVariable UUID idRota) {
    return ResponseEntity.ok(service.listByRota(idRota));
  }

  @GetMapping("/{idWaypoint}")
  public ResponseEntity<WaypointDTO> get(@PathVariable UUID idRota,
      @PathVariable UUID idWaypoint) {
    return ResponseEntity.ok(service.getById(idRota, idWaypoint));
  }

  @PutMapping("/{idWaypoint}")
  public ResponseEntity<WaypointDTO> update(@PathVariable UUID idRota,
      @PathVariable UUID idWaypoint,
      @RequestBody @Validated WaypointUpdateDTO dto) {
    return ResponseEntity.ok(service.update(idRota, idWaypoint, dto));
  }

  @DeleteMapping("/{idWaypoint}")
  public ResponseEntity<Void> delete(@PathVariable UUID idRota,
      @PathVariable UUID idWaypoint) {
    service.delete(idRota, idWaypoint);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reorder")
  public ResponseEntity<Void> reorder(@PathVariable UUID idRota,
      @RequestBody @Validated ReorderDTO dto) {
    service.reorder(idRota, dto);
    return ResponseEntity.noContent().build();
  }
}
