package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.RotaWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RotaWaypointRepository extends JpaRepository<RotaWaypoint, UUID> {

}
