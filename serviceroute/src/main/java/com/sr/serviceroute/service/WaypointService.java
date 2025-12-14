package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.WaypointCreateDTO;
import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.repository.WaypointRepository;
import org.springframework.stereotype.Service;

@Service
public class WaypointService {
  private final WaypointRepository waypointRepository;

  WaypointService(WaypointRepository waypointRepository) {
    this.waypointRepository = waypointRepository;
  }

  public Waypoint criar(WaypointCreateDTO dto) {
    Waypoint waypoint = Waypoint.builder()
        .latitude(dto.latitude())
        .longitude(dto.longitude())
        .endereco(dto.endereco())
        .build();

    return waypointRepository.save(waypoint);
  }
}
