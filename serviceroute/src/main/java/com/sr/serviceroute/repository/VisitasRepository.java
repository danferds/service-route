package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.Visitas;
import com.sr.serviceroute.model.enums.AlertaTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VisitasRepository extends JpaRepository<Visitas, UUID> {
  boolean existsByViagemIdAndRotaWaypointId(UUID viagemId, UUID rotaWaypointId);

  List<Visitas> findByViagemIdOrderByDataCriacaoAsc(UUID viagemId);

  @Query("""
      select v from Visitas v
      where v.etaReal is not null
      and v.rotaWaypoint.etaPrevisto is not null
      and not exists (
        select 1 from Alertas a
        where a.visita = v and a.tipo = :tipo
      )
      """)
  List<Visitas> buscarVisitasElegiveisParaAlerta(@Param("tipo") AlertaTipo tipo);
}
