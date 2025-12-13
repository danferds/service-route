package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.EventoRastreamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface EventoRastreamentoRepository extends JpaRepository<EventoRastreamento, Long> {

  @Query("SELECT e FROM EventoRastreamento e WHERE e.instanciaRotaId = :instanciaId AND e.gravadoEm >= :since ORDER BY e.gravadoEm ASC")
  List<EventoRastreamento> findRecentByInstancia(@Param("instanciaId") java.util.UUID instanciaId,
      @Param("since") OffsetDateTime since);

  @Query("SELECT DISTINCT e.instanciaRotaId FROM EventoRastreamento e WHERE e.gravadoEm >= :since")
  List<java.util.UUID> findActiveInstancias(@Param("since") OffsetDateTime since);
}