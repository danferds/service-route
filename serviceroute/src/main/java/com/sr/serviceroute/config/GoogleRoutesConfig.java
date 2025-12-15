package com.sr.serviceroute.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.routes")
@Data
public class GoogleRoutesConfig {

  private String baseUrl;
  private String apiKey;
  private int timeoutMillis;
}
