package com.sr.serviceroute.service.planning.model;

import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class ResultadoPlanejamento {
  private Duration tempoTotal;
  private List<ResultadoWaypoint> waypoints;
}
