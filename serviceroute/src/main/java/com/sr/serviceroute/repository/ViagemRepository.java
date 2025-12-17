package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ViagemRepository extends JpaRepository<Viagem, UUID> {
  List<Viagem> findByRotaId(UUID rotaId);
}
