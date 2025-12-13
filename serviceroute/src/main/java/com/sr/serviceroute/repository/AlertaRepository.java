package com.sr.serviceroute.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.serviceroute.model.Alerta;

import java.util.UUID;

public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
}
