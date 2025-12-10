package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "route_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePlan {
  @Id
  private UUID id;

  @Column(name = "id_rota")
  private UUID rotaId;

  @Column(name = "instancia_rota_id")
  private UUID instanciaRotaId;

  private String status;
  private String solver;
  private String objective;

  @Column(columnDefinition = "jsonb")
  private String params;

  @Column(name = "total_distance_m")
  private Double totalDistanceM;

  @Column(name = "total_time_seconds")
  private Integer totalTimeSeconds;

  @Column(name = "plan_json", columnDefinition = "jsonb")
  private String planJson;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "criado_em")
  private OffsetDateTime criadoEm;

  @Column(name = "atualizado_em")
  private OffsetDateTime atualizadoEm;

  @Column(name = "applied_at")
  private OffsetDateTime appliedAt;
}
