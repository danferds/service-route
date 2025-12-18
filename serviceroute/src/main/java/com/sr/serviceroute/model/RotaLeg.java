package com.sr.serviceroute.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rota_legs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotaLeg {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_rota", nullable = false)
  private Rota rota;

  @Column(name = "distance_meters")
  private Long distanceMeters;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "encoded_polyline", columnDefinition = "TEXT")
  private String encodedPolyline;

  @Column(name = "start_lat")
  private Double startLat;

  @Column(name = "start_lng")
  private Double startLng;

  @Column(name = "end_lat")
  private Double endLat;

  @Column(name = "end_lng")
  private Double endLng;

  @Column(nullable = false)
  private Integer seq;
}
