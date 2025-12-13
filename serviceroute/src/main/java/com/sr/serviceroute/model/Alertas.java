package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

import com.sr.serviceroute.model.enums.AlertaTipo;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alertas {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_visita", nullable = false)
  private Visitas visita;

  @Column(nullable = false)
  private String texto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertaTipo tipo;

  @Column(name = "data_criacao", nullable = false)
  private Instant dataCriacao;

  @PrePersist
  public void prePersist() {
    this.dataCriacao = Instant.now();
  }
}
