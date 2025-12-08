package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "eventos_rastreamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoRastreamento {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "instancia_rota_id")
  private java.util.UUID instanciaRotaId;

  @Column(name = "gravado_em")
  private OffsetDateTime gravadoEm;

  @Column(name = "id_dispositivo")
  private String dispositivoId;

  private Double latitude;
  private Double longitude;
  private Float velocidade;
  private Float heading;

  @Column(columnDefinition = "jsonb")
  private String metadata;

  private OffsetDateTime criadoEm;
}
