package com.sr.serviceroute.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sr.serviceroute.model.InstanciaRota;

import java.util.UUID;

public interface InstanciaRotaRepository extends JpaRepository<InstanciaRota, UUID> {
}
