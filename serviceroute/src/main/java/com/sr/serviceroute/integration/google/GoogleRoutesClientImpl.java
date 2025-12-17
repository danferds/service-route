package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;
import lombok.RequiredArgsConstructor;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GoogleRoutesClientImpl implements GoogleRoutesClient {

  private final WebClient googleRoutesWebClient;

  @Override
  @Retry(name = "googleRoutes")
  @CircuitBreaker(name = "googleRoutes")
  public GoogleRouteResponseDTO calcularRota(
      GoogleRouteRequestDTO request) {
    return googleRoutesWebClient
        .post()
        .uri("/directions/v2:computeRoutes")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(GoogleRouteResponseDTO.class)
        .block();
  }
}
