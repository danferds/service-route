package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.CriarRotaDTO;
import com.sr.serviceroute.dto.CriarRotaResponseDTO;
import com.sr.serviceroute.service.RotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rotas")
public class RotaController {
  private final RotaService rotaService;

  public RotaController(RotaService rotaService) {
    this.rotaService = rotaService;
  }

  /**
   * Cria uma nova rota com origem, destino e pontos intermediários.
   *
   * @param dto dados de criação da rota
   * @return id da rota criada
   */
  @PostMapping
  public ResponseEntity<CriarRotaResponseDTO> criarRota(
      @Valid @RequestBody CriarRotaDTO dto) {

    UUID rotaId = rotaService.criarRota(dto);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(new CriarRotaResponseDTO(rotaId));
  }

  @PostMapping("/{id}/planejar")
  public ResponseEntity<Void> planejar(@PathVariable UUID id) {
    rotaService.planejarRota(id);
    return ResponseEntity.accepted().build();
  }
}
