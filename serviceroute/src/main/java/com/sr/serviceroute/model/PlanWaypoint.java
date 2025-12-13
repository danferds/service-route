package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "plan_waypoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanWaypoint {
  @Id
  private UUID id;

  @Column(name = "route_plan_id")
  private UUID routePlanId;

  private Integer seq;
  private String ref; // existing id (string) or tempId
  private String type; // 'existing' | 'inline'
  private String nome;
  private Double latitude;
  private Double longitude;
  private OffsetDateTime eta;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;
}
