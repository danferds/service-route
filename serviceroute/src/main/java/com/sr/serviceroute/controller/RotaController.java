package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.*;
import com.sr.serviceroute.service.RotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rotas")
public class RotaController {

  private final RotaService service;

  public RotaController(RotaService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<RotaDTO> create(@RequestBody @Valid RotaCreateDTO dto) {
    RotaDTO created = service.create(dto);
    URI location = URI.create("/api/rotas/" + created.id());
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping
  public ResponseEntity<List<RotaDTO>> list() {
    return ResponseEntity.ok(service.list());
  }

  @GetMapping("/{id}")
  public ResponseEntity<RotaDTO> get(@PathVariable UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @GetMapping("/owner/{ownerId}")
  public ResponseEntity<List<RotaDTO>> listByOwner(@PathVariable UUID ownerId) {
    return ResponseEntity.ok(service.listByOwner(ownerId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RotaDTO> update(@PathVariable UUID id,
      @RequestBody @Valid RotaUpdateDTO dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
