package com.sr.serviceroute.service;

import com.sr.serviceroute.dto.*;
import com.sr.serviceroute.model.Usuario;
import com.sr.serviceroute.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

  private final UsuarioRepository repo;

  public UsuarioService(UsuarioRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public UsuarioDTO create(UsuarioCreateDTO dto) {
    Usuario u = Usuario.builder()
        .id(UUID.randomUUID())
        .nome(dto.nome())
        .email(dto.email())
        .criadoEm(OffsetDateTime.now())
        .build();
    repo.save(u);
    return toDTO(u);
  }

  public UsuarioDTO get(UUID id) {
    Usuario u = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    return toDTO(u);
  }

  public List<UsuarioDTO> list() {
    return repo.findAll().stream().map(this::toDTO).toList();
  }

  @Transactional
  public UsuarioDTO update(UUID id, UsuarioUpdateDTO dto) {
    Usuario u = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    if (dto.nome() != null)
      u.setNome(dto.nome());
    if (dto.email() != null)
      u.setEmail(dto.email());

    repo.save(u);
    return toDTO(u);
  }

  @Transactional
  public void delete(UUID id) {
    if (!repo.existsById(id))
      throw new ResourceNotFoundException("Usuário não encontrado");
    repo.deleteById(id);
  }

  private UsuarioDTO toDTO(Usuario u) {
    return new UsuarioDTO(u.getId(), u.getNome(), u.getEmail(), u.getCriadoEm());
  }

}
