package com.sr.serviceroute.service.planning.model;

import com.sr.serviceroute.model.RotaWaypoint;
import lombok.Data;

import java.time.Instant;

@Data
public class ResultadoWaypoint {

  private RotaWaypoint waypoint;
  private int seq;
  private Instant etaPrevisto;

}
