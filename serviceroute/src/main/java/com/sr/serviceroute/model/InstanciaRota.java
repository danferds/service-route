package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "instancias_rota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstanciaRota {
  @Id
  private UUID id;

  @Column(name = "id_rota")
  private UUID rotaId;

  @Column(name = "id_veiculo")
  private String veiculoId;

  @Column(name = "id_atual_waypoint")
  private UUID atualWaypointId;

  private OffsetDateTime iniciadoEm;
  private OffsetDateTime finalizadoEm;
  private String status;
  private OffsetDateTime criadoEm;
}
