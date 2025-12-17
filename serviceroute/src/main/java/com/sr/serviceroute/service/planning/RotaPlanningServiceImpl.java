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
      // Reconstrói a lista lógica de waypoints na ordem enviada ao Google (Origem ->
      // Intermediários -> Destino)
      List<RotaWaypoint> orderedWaypoints = new java.util.ArrayList<>();

      RotaWaypoint origin = waypoints.stream()
          .filter(w -> w.getTipo() == com.sr.serviceroute.model.enums.RotaWaypointTipo.ORIGEM)
          .findFirst()
          .orElse(null);
      if (origin != null)
        orderedWaypoints.add(origin);

      waypoints.stream()
          .filter(w -> w.getTipo() != com.sr.serviceroute.model.enums.RotaWaypointTipo.ORIGEM
              && w.getTipo() != com.sr.serviceroute.model.enums.RotaWaypointTipo.DESTINO)
          .forEach(orderedWaypoints::add);

      RotaWaypoint dest = waypoints.stream()
          .filter(w -> w.getTipo() == com.sr.serviceroute.model.enums.RotaWaypointTipo.DESTINO)
          .findFirst()
          .orElse(null);
      if (dest != null)
        orderedWaypoints.add(dest);

      List<com.sr.serviceroute.service.planning.model.ResultadoWaypoint> results = resultadoPlanejamento.getWaypoints();

      if (orderedWaypoints.size() != results.size()) {
        log.warn("Mismatch entre quantidade de waypoints ({}) e resultados ({})",
            orderedWaypoints.size(), results.size());
      }

      log.info("Waypoints Ordenados para match:");
      for (int k = 0; k < orderedWaypoints.size(); k++) {
        log.info("  [{}] Type: {}, ID: {}", k, orderedWaypoints.get(k).getTipo(), orderedWaypoints.get(k).getId());
      }

      for (int i = 0; i < Math.min(orderedWaypoints.size(), results.size()); i++) {
        RotaWaypoint wp = orderedWaypoints.get(i);
        var res = results.get(i);
        if (res != null) {
          log.info("Aplicando Resultado [Index {}] (Seq {}) no Waypoint ID {}", i, res.getSeq(), wp.getId());
          wp.setSeq(res.getSeq());
          wp.setEtaPrevisto(res.getEtaPrevisto());
        }
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
