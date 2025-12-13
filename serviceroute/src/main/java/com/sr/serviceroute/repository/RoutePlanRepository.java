package com.sr.serviceroute.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.serviceroute.model.RoutePlan;

import java.util.List;
import java.util.UUID;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, UUID> {
  List<RoutePlan> findByRotaId(UUID rotaId);

  List<RoutePlan> findByInstanciaRotaId(UUID instanciaId);
}
