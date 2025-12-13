package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "waypoint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waypoint {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false)
  private Double latitude;

  private String endereco;
}
