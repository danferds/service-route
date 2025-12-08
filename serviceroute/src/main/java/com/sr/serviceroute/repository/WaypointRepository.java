package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {
  @Query("SELECT w FROM Waypoint w WHERE w.rotaId = :rotaId ORDER BY w.seq ASC")
  List<Waypoint> findByRotaOrderBySeq(@Param("rotaId") UUID rotaId);
}
