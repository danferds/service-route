package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.sr.serviceroute.model.enums.RotaWaypointTipo;

@Entity
@Table(name = "rota_waypoint", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "id_rota", "seq" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotaWaypoint {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_rota", nullable = false)
  private Rota rota;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_waypoint", nullable = false)
  private Waypoint waypoint;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "tipo", columnDefinition = "rota_waypoint_tipo", nullable = false)
  private RotaWaypointTipo tipo;

  private Integer seq;

  @Column(name = "eta_previsto")
  private Instant etaPrevisto;
}
