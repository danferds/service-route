package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;

public interface GoogleRoutesClient {

  GoogleRouteResponseDTO calcularRota(
      GoogleRouteRequestDTO request);
}
