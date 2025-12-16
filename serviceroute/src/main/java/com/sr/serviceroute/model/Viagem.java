package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "viagem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viagem {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_rota", nullable = false)
  private Rota rota;

  @Column(name = "veiculo_ref")
  private String veiculoRef;

  @Column(name = "tempo_real_total")
  private Integer tempoRealTotal;

  @Column(name = "data_inicio")
  private Instant dataInicio;

  @Column(name = "data_fim")
  private Instant dataFim;
}
