package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.RegistrarVisitaDTO;
import com.sr.serviceroute.dto.VisitaResponseDTO;
import com.sr.serviceroute.service.VisitasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/viagens/{viagemId}/visitas")
@RequiredArgsConstructor
public class VisitasController {

  private final VisitasService visitasService;

  @PostMapping
  public ResponseEntity<VisitaResponseDTO> registrar(@PathVariable UUID viagemId,
                                                     @Valid @RequestBody RegistrarVisitaDTO dto) {
    VisitaResponseDTO response = visitasService.registrar(viagemId, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<VisitaResponseDTO>> listar(@PathVariable UUID viagemId) {
    return ResponseEntity.ok(visitasService.listarPorViagem(viagemId));
  }
}
