package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {
  @Id
  private UUID id;

  @Column(name = "instancia_rota_id")
  private UUID instanciaRotaId;

  @Column(name = "id_waypoint")
  private UUID waypointId;

  private String tipo;
  private String message;
  private OffsetDateTime criadoEm;
  private Boolean resolvido;
  private OffsetDateTime resolvidoEm;
}
