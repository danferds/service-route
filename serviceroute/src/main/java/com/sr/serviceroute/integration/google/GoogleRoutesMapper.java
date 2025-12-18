package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.model.RotaWaypoint;
import com.sr.serviceroute.service.planning.model.ResultadoPlanejamento;

import java.util.List;

public interface GoogleRoutesMapper {

    GoogleRouteRequestDTO toGoogleRequest(
            Rota rota,
            List<RotaWaypoint> waypoints);

    ResultadoPlanejamento fromGoogleResponse(
            GoogleRouteResponseDTO response);

    java.util.List<com.sr.serviceroute.model.RotaLeg> mapLegs(GoogleRouteResponseDTO response, Rota rota);
}
