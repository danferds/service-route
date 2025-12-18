package com.sr.serviceroute.controller;

import com.sr.serviceroute.dto.RelatorioViagemDTO;
import com.sr.serviceroute.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

  private final RelatorioService relatorioService;

  @GetMapping("/viagens-em-andamento")
  public ResponseEntity<List<RelatorioViagemDTO>> obterRelatorioViagensEmAndamento() {
    return ResponseEntity.ok(relatorioService.gerarRelatorioViagensEmAndamento());
  }
}
