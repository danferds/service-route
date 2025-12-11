package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.*;
import com.sr.serviceroute.model.Rota;
import com.sr.serviceroute.repository.RotaRepository;
import com.sr.serviceroute.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RotaService {

  private final RotaRepository repo;
  private final UsuarioRepository usuarioRepo;

  public RotaService(RotaRepository repo, UsuarioRepository usuarioRepo) {
    this.repo = repo;
    this.usuarioRepo = usuarioRepo;
  }

  @Transactional
  public RotaDTO create(RotaCreateDTO dto) {

    if (dto.ownerId() != null && !usuarioRepo.existsById(dto.ownerId())) {
      throw new BadRequestException("ownerId inválido: usuário não encontrado.");
    }

    Rota r = Rota.builder()
        .id(UUID.randomUUID())
        .ownerId(dto.ownerId())
        .nome(dto.nome())
        .descricao(dto.descricao())
        .criadoEm(OffsetDateTime.now())
        .atualizadoEm(OffsetDateTime.now())
        .build();

    repo.save(r);
    return toDTO(r);
  }

  public RotaDTO get(UUID id) {
    Rota r = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada"));
    return toDTO(r);
  }

  public List<RotaDTO> list() {
    return repo.findAll().stream().map(this::toDTO).toList();
  }

  public List<RotaDTO> listByOwner(UUID ownerId) {
    return repo.findByOwnerId(ownerId).stream().map(this::toDTO).toList();
  }

  @Transactional
  public RotaDTO update(UUID id, RotaUpdateDTO dto) {
    Rota r = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada"));

    if (dto.nome() != null)
      r.setNome(dto.nome());
    if (dto.descricao() != null)
      r.setDescricao(dto.descricao());

    r.setAtualizadoEm(OffsetDateTime.now());

    repo.save(r);
    return toDTO(r);
  }

  @Transactional
  public void delete(UUID id) {
    if (!repo.existsById(id))
      throw new ResourceNotFoundException("Rota não encontrada");
    repo.deleteById(id);
  }

  private RotaDTO toDTO(Rota r) {
    return new RotaDTO(
        r.getId(),
        r.getOwnerId(),
        r.getNome(),
        r.getDescricao(),
        r.getCriadoEm(),
        r.getAtualizadoEm());
  }
}
