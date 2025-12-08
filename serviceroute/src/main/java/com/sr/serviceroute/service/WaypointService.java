package com.sr.serviceroute.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sr.serviceroute.model.Waypoint;
import com.sr.serviceroute.repository.WaypointRepository;
import com.sr.serviceroute.dto.ReorderDTO;
import com.sr.serviceroute.dto.ReorderItem;
import com.sr.serviceroute.dto.WaypointCreateDTO;
import com.sr.serviceroute.dto.WaypointDTO;
import com.sr.serviceroute.dto.WaypointUpdateDTO;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WaypointService {

  private final WaypointRepository waypointRepo;
  private final EntityManager em;

  public WaypointService(WaypointRepository waypointRepo, EntityManager em) {
    this.waypointRepo = waypointRepo;
    this.em = em;
  }

  public List<WaypointDTO> listByRota(UUID idRota) {
    List<Waypoint> list = waypointRepo.findByRotaOrderBySeq(idRota);
    return list.stream().map(this::toDto).collect(Collectors.toList());
  }

  public WaypointDTO getById(UUID idRota, UUID idWaypoint) {
    Waypoint wp = waypointRepo.findById(idWaypoint)
        .orElseThrow(() -> new ResourceNotFoundException("Waypoint nao encontrado"));
    if (!Objects.equals(wp.getRotaId(), idRota))
      throw new ResourceNotFoundException("Waypoint não encontrado na rota");
    return toDto(wp);
  }

  @Transactional
  public WaypointDTO create(UUID idRota, WaypointCreateDTO dto) {
    validateCoords(dto.latitude(), dto.longitude());
    int seq = Optional.ofNullable(dto.seq()).orElseGet(() -> {
      // if no seq provided, place at end
      Integer maxSeq = waypointRepo.findByRotaOrderBySeq(idRota).stream()
          .map(Waypoint::getSeq).max(Integer::compareTo).orElse(0);
      return maxSeq + 1;
    });
    if (seq < 1)
      throw new BadRequestException("seq must be >= 1");

    // lock waypoints for this rota to avoid races during shift
    List<Waypoint> lock = em
        .createQuery("SELECT w FROM Waypoint w WHERE w.rotaId = :rotaId ORDER BY w.seq", Waypoint.class)
        .setParameter("rotaId", idRota)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .getResultList();

    // shift seq >= seq
    for (Waypoint w : lock) {
      if (w.getSeq() >= seq) {
        w.setSeq(w.getSeq() + 1);
        waypointRepo.save(w);
      }
    }

    Waypoint wp = Waypoint.builder()
        .id(UUID.randomUUID())
        .rotaId(idRota)
        .seq(seq)
        .nome(dto.nome())
        .latitude(dto.latitude())
        .longitude(dto.longitude())
        .eta(dto.eta())
        .criadoEm(OffsetDateTime.now())
        .build();

    waypointRepo.save(wp);
    return toDto(wp);
  }

  @Transactional
  public WaypointDTO update(UUID idRota, UUID idWaypoint, WaypointUpdateDTO dto) {
    validateCoords(dto.latitude(), dto.longitude());
    Waypoint wp = waypointRepo.findById(idWaypoint)
        .orElseThrow(() -> new ResourceNotFoundException("Waypoint nao encontrado"));
    if (!Objects.equals(wp.getRotaId(), idRota))
      throw new ResourceNotFoundException("Waypoint nao encontrado na rota");

    Integer newSeq = dto.seq();
    if (newSeq != null && newSeq < 1)
      throw new BadRequestException("seq must be >= 1");

    if (newSeq != null && !newSeq.equals(wp.getSeq())) {
      // lock and shift other waypoints
      List<Waypoint> lock = em
          .createQuery("SELECT w FROM Waypoint w WHERE w.rotaId = :rotaId ORDER BY w.seq", Waypoint.class)
          .setParameter("rotaId", idRota)
          .setLockMode(LockModeType.PESSIMISTIC_WRITE)
          .getResultList();

      // remove this wp from list for easier logic
      lock.removeIf(x -> x.getId().equals(wp.getId()));

      // shift logic: remove gap and then insert at newSeq
      // decrement sequences >= oldSeq
      int oldSeq = wp.getSeq();
      for (Waypoint w : lock) {
        if (w.getSeq() > oldSeq) {
          w.setSeq(w.getSeq() - 1);
          waypointRepo.save(w);
        }
      }
      // shift up seq >= newSeq
      for (Waypoint w : lock) {
        if (w.getSeq() >= newSeq) {
          w.setSeq(w.getSeq() + 1);
          waypointRepo.save(w);
        }
      }
      wp.setSeq(newSeq);
    }

    // other fields
    if (dto.nome() != null)
      wp.setNome(dto.nome());
    if (dto.latitude() != null)
      wp.setLatitude(dto.latitude());
    if (dto.longitude() != null)
      wp.setLongitude(dto.longitude());
    if (dto.eta() != null)
      wp.setEta(dto.eta());

    waypointRepo.save(wp);
    return toDto(wp);
  }

  @Transactional
  public void delete(UUID idRota, UUID idWaypoint) {
    Waypoint wp = waypointRepo.findById(idWaypoint)
        .orElseThrow(() -> new ResourceNotFoundException("Waypoint nao encontrado"));
    if (!Objects.equals(wp.getRotaId(), idRota))
      throw new ResourceNotFoundException("Waypoint nao encontrado na rota");

    // lock waypoints
    List<Waypoint> lock = em
        .createQuery("SELECT w FROM Waypoint w WHERE w.rotaId = :rotaId ORDER BY w.seq", Waypoint.class)
        .setParameter("rotaId", idRota)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .getResultList();

    int removedSeq = wp.getSeq();
    waypointRepo.delete(wp);

    // decrement seq > removedSeq
    for (Waypoint w : lock) {
      if (w.getSeq() > removedSeq) {
        w.setSeq(w.getSeq() - 1);
        waypointRepo.save(w);
      }
    }
  }

  @Transactional
  public void reorder(UUID idRota, ReorderDTO dto) {
    List<ReorderItem> items = dto.items();
    if (CollectionUtils.isEmpty(items))
      return;

    // validate uniqueness of ids and seqs in payload
    Set<UUID> ids = new HashSet<>();
    Set<Integer> seqs = new HashSet<>();
    for (ReorderItem it : items) {
      if (!ids.add(it.id()))
        throw new BadRequestException("esse id já existe");
      if (!seqs.add(it.seq()))
        throw new BadRequestException("esse seq ja existe");
      if (it.seq() < 1)
        throw new BadRequestException("seq deve ser maior ou igual a 1");
    }

    // fetch and lock all waypoints for rota
    List<Waypoint> lock = em.createQuery("SELECT w FROM Waypoint w WHERE w.rotaId = :rotaId", Waypoint.class)
        .setParameter("rotaId", idRota)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
        .getResultList();

    Map<UUID, Waypoint> map = lock.stream().collect(Collectors.toMap(Waypoint::getId, w -> w));
    // ensure payload ids belong to rota
    for (ReorderItem it : items) {
      if (!map.containsKey(it.id()))
        throw new BadRequestException("waypoint id nao pertence a rota rota: " + it.id());
    }

    // apply updates
    for (ReorderItem it : items) {
      Waypoint w = map.get(it.id());
      w.setSeq(it.seq());
      waypointRepo.save(w);
    }
  }

  private WaypointDTO toDto(Waypoint w) {
    return new WaypointDTO(w.getId(), w.getSeq(), w.getNome(), w.getLatitude(), w.getLongitude(), w.getEta());
  }

  private void validateCoords(Double latitude, Double longitude) {
    if (latitude == null || longitude == null)
      throw new BadRequestException("latitude e longitude sao obrigatorios");
    if (latitude < -90 || latitude > 90)
      throw new BadRequestException("latitude fora do intervalo permitido");
    if (longitude < -180 || longitude > 180)
      throw new BadRequestException("longitude fora do intervalo permitido");
  }
}
