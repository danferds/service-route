package com.sr.serviceroute.service.planning;

import com.sr.serviceroute.integration.google.GoogleRoutesClient;
import com.sr.serviceroute.integration.google.GoogleRoutesMapper;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.repository.RotaRepository;
import com.sr.serviceroute.repository.RotaWaypointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RotaPlanningServiceImpl implements RotaPlanningService {
  private final RotaRepository rotaRepository;
  private final RotaWaypointRepository rotaWaypointRepository;
  private final GoogleRoutesClient googleRoutesClient;
  private final GoogleRoutesMapper googleRoutesMapper;

  @Override
  public void planejar(UUID rotaId) {

    // 1. Buscar rota
    Rota rota = rotaRepository.findById(rotaId)
        .orElseThrow(() -> new IllegalArgumentException("Rota não encontrada"));

    // 2. Buscar waypoints da rota
    List<RotaWaypoint> waypoints = rotaWaypointRepository.findByRotaIdOrderBySeqAsc(rotaId);

    // 3. Montar request para API externa (via mapper)
    var googleRequest = googleRoutesMapper.toGoogleRequest(rota, waypoints);

    log.error("Google Routes request: {}", googleRequest);

    try {
      // 4. Chamar API externa
      var googleResponse = googleRoutesClient.calcularRota(googleRequest);

      log.error("Google Routes response: {}", googleResponse);

      // 5. Interpretar resposta
      var resultadoPlanejamento = googleRoutesMapper.fromGoogleResponse(googleResponse);

      // 6. Atualizar waypoints (ordem e ETA)
      for (var resultado : resultadoPlanejamento.getWaypoints()) {
        log.debug("resultado: {}", resultado);
        Integer seq = resultado.getSeq();
        if (seq == null || seq < 0 || seq >= waypoints.size()) {
          log.warn("Seq inválido no resultado do planejamento: {}", seq);
          continue;
        }
        RotaWaypoint waypoint = waypoints.get(seq);
        if (waypoint == null) {
          log.warn("Waypoint não encontrado para seq={}", seq);
          continue;
        }
        waypoint.setSeq(resultado.getSeq());
        waypoint.setEtaPrevisto(resultado.getEtaPrevisto());
      }

      // // 7. Atualizar rota
      var duracao = resultadoPlanejamento.getTempoTotal();
      rota.setTempoEstimadoTotal(
          duracao == null ? null : Math.toIntExact(duracao.getSeconds()));

    } catch (WebClientResponseException ex) {
      log.error("Google Routes API error: status={} body={}", ex.getStatusText(),
          ex.getResponseBodyAsString());
      throw ex;
    }
    // Persistência acontece via contexto transacional
  }
}
