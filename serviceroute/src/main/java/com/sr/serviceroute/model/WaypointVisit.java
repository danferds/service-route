package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "waypoint_visits", uniqueConstraints = @UniqueConstraint(columnNames = { "instancia_rota_id",
    "id_waypoint" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaypointVisit {
  @Id
  private UUID id;

  @Column(name = "instancia_rota_id")
  private UUID instanciaRotaId;

  @Column(name = "id_waypoint")
  private UUID waypointId;

  @Column(name = "chegou_em")
  private OffsetDateTime chegouEm;

  @Column(name = "criado_por")
  private String criadoPor;

  @Column(name = "distancia_metros")
  private Double distanciaMetros;

  private OffsetDateTime criadoEm;
}
