package com.sr.serviceroute.service;

import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.model.enums.RotaWaypointTipo;
import com.sr.serviceroute.repository.RotaWaypointRepository;
import org.springframework.stereotype.Service;

@Service
public class RotaWaypointService {
  private final RotaWaypointRepository rotaWaypointRepository;

  RotaWaypointService(RotaWaypointRepository rotaWaypointRepository) {
    this.rotaWaypointRepository = rotaWaypointRepository;
  }

  public void vincular(Rota rota,
      Waypoint waypoint,
      RotaWaypointTipo tipo,
      Integer sequencia) {

    RotaWaypoint rw = RotaWaypoint.builder()
        .rota(rota)
        .waypoint(waypoint)
        .tipo(tipo)
        .seq(sequencia)
        .build();

    rotaWaypointRepository.save(rw);
  }
}
