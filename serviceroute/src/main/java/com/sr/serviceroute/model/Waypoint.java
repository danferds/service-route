package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "waypoints", uniqueConstraints = @UniqueConstraint(columnNames = { "id_rota", "seq" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waypoint {
  @Id
  private UUID id;

  @Column(name = "id_rota", nullable = false)
  private UUID rotaId;

  private Integer seq;
  private String nome;
  private Double latitude;
  private Double longitude;
  private OffsetDateTime eta;
  private OffsetDateTime criadoEm;
}
