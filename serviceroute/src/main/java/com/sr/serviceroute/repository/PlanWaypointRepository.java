package com.sr.serviceroute.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.serviceroute.model.PlanWaypoint;

import java.util.List;
import java.util.UUID;

public interface PlanWaypointRepository extends JpaRepository<PlanWaypoint, UUID> {
  List<PlanWaypoint> findByRoutePlanIdOrderBySeqAsc(UUID routePlanId);

  void deleteByRoutePlanId(UUID routePlanId);
}
