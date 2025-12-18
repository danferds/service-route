package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.sr.serviceroute.model.enums.RotaStatus;

@Entity
@Table(name = "rota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rota {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @Column(nullable = false)
  private String nome;

  @Column(name = "data_criacao", nullable = false)
  private Instant dataCriacao;

  @Column(name = "data_atualizacao", nullable = false)
  private Instant dataAtualizacao;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "rota_status", nullable = false)
  private RotaStatus status;

  @Column(name = "tempo_estimado_total")
  private Integer tempoEstimadoTotal;

  @OneToMany(mappedBy = "rota", cascade = CascadeType.ALL, orphanRemoval = true)
  private java.util.List<RotaLeg> legs;

  @PrePersist
  public void prePersist() {
    this.dataCriacao = Instant.now();
    this.dataAtualizacao = Instant.now();
    this.status = RotaStatus.CRIADA;
  }

  @PreUpdate
  public void preUpdate() {
    this.dataAtualizacao = Instant.now();
  }
}
