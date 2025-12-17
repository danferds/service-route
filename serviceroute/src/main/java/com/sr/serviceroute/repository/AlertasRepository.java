package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.Alertas;
import com.sr.serviceroute.model.enums.AlertaTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertasRepository extends JpaRepository<Alertas, UUID> {
  boolean existsByVisitaIdAndTipo(UUID visitaId, AlertaTipo tipo);
}
