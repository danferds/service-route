package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visitas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visitas {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_viagem", nullable = false)
  private Viagem viagem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_rota_waypoint", nullable = false)
  private RotaWaypoint rotaWaypoint;

  @Column(name = "eta_real")
  private Instant etaReal;

  @Column(name = "data_criacao", nullable = false)
  private Instant dataCriacao;

  @PrePersist
  public void prePersist() {
    this.dataCriacao = Instant.now();
  }
}
