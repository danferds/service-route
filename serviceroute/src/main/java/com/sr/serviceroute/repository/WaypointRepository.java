package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaypointRepository extends JpaRepository<Waypoint, UUID> {

}
