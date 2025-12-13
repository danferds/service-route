package com.sr.serviceroute.dto;

import java.util.List;
import java.util.UUID;

public record CreatePlanDTO(
    UUID instanciaRotaId,
    String objective,
    List<String> waypointIds,
    List<InlineWaypointDTO> inlineWaypoints,
    String paramsJson) {
}
