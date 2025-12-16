package com.sr.serviceroute.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

  private final GoogleRoutesConfig config;

  @Bean
  public WebClient googleRoutesWebClient() {
    return WebClient.builder()
        .baseUrl(config.getBaseUrl())
        .defaultHeader("X-Goog-Api-Key", config.getApiKey())
        .defaultHeader("X-Goog-FieldMask",
            "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs.distanceMeters,routes.legs.duration,routes.legs.polyline,routes.legs.startLocation,routes.legs.endLocation,routes.optimizedIntermediateWaypointIndex")
        .defaultHeader("Content-Type", "application/json")
        .build();
  }
}
