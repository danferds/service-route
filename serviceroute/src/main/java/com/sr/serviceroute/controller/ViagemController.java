package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.CriarViagemDTO;
import com.sr.serviceroute.dto.FinalizarViagemDTO;
import com.sr.serviceroute.dto.IniciarViagemDTO;
import com.sr.serviceroute.dto.RotaCompletaDTO;
import com.sr.serviceroute.dto.ViagemResponseDTO;
import com.sr.serviceroute.service.ViagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/viagens")
@RequiredArgsConstructor
public class ViagemController {

  private final ViagemService viagemService;

  @PostMapping
  public ResponseEntity<ViagemResponseDTO> criar(@Valid @RequestBody CriarViagemDTO dto) {
    ViagemResponseDTO response = viagemService.criar(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/{id}/inicio")
  public ResponseEntity<ViagemResponseDTO> iniciar(@PathVariable UUID id,
                                                   @RequestBody(required = false) IniciarViagemDTO dto) {
    return ResponseEntity.ok(viagemService.iniciar(id, dto));
  }

  @PostMapping("/{id}/fim")
  public ResponseEntity<ViagemResponseDTO> finalizar(@PathVariable UUID id,
                                                     @RequestBody(required = false) FinalizarViagemDTO dto) {
    return ResponseEntity.ok(viagemService.finalizar(id, dto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ViagemResponseDTO> buscar(@PathVariable UUID id) {
    return ResponseEntity.ok(viagemService.buscar(id));
  }

  @GetMapping("/{id}/rota")
  public ResponseEntity<RotaCompletaDTO> rotaCompleta(@PathVariable UUID id) {
    return ResponseEntity.ok(viagemService.rotaCompletaDaViagem(id));
  }

  @GetMapping
  public ResponseEntity<List<ViagemResponseDTO>> listarPorRota(@RequestParam("rotaId") UUID rotaId) {
    return ResponseEntity.ok(viagemService.listarPorRota(rotaId));
  }
}
