package com.sr.serviceroute.repository;

import com.sr.serviceroute.model.Rota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RotaRepository extends JpaRepository<Rota, UUID> {

  List<Rota> findByOwnerId(UUID ownerId);
}
