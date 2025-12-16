package com.sr.serviceroute.integration.google;

import com.sr.serviceroute.integration.google.dto.GoogleRouteRequestDTO;
import com.sr.serviceroute.integration.google.dto.GoogleRouteResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class GoogleRoutesClientImpl implements GoogleRoutesClient {

  private final WebClient googleRoutesWebClient;

  @Override
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
