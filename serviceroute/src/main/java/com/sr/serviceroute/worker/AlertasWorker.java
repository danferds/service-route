package com.sr.serviceroute.worker;

import com.sr.serviceroute.service.VisitasMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertasWorker {

  private final VisitasMonitorService visitasMonitorService;

  @Scheduled(fixedDelayString = "${alerts.worker.delay-ms:60000}")
  public void verificarAtrasos() {
    try {
      visitasMonitorService.processarAtrasosPendentes();
    } catch (Exception ex) {
      log.error("Erro ao processar alertas de atraso", ex);
    }
  }
}
