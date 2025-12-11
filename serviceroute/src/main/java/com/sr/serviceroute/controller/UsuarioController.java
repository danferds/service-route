package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.*;
import com.sr.serviceroute.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

  private final UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<UsuarioDTO> create(@RequestBody @Valid UsuarioCreateDTO dto) {
    UsuarioDTO created = service.create(dto);
    URI location = URI.create("/api/usuarios/" + created.id());
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping
  public ResponseEntity<List<UsuarioDTO>> list() {
    return ResponseEntity.ok(service.list());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UsuarioDTO> get(@PathVariable UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UsuarioDTO> update(@PathVariable UUID id,
      @RequestBody @Valid UsuarioUpdateDTO dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
