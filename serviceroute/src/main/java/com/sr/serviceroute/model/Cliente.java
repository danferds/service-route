package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String nome;
}
