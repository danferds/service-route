package com.sr.serviceroute.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.serviceroute.model.WaypointVisit;

import java.util.UUID;

public interface WaypointVisitRepository extends JpaRepository<WaypointVisit, UUID> {
}
