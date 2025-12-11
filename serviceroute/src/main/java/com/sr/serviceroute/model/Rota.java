package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rotas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rota {

  @Id
  private UUID id;

  @Column(name = "owner_id")
  private UUID ownerId;

  private String nome;

  private String descricao;

  @Column(name = "criado_em")
  private OffsetDateTime criadoEm;

  @Column(name = "atualizado_em")
  private OffsetDateTime atualizadoEm;
}
