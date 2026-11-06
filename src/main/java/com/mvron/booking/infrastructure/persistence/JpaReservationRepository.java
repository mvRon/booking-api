package com.mvron.booking.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.reservation.Reservation;
import com.mvron.booking.domain.reservation.ReservationRepository;
import com.mvron.booking.domain.timeslot.TimeSlot;

@Repository
public class JpaReservationRepository implements ReservationRepository {

    private final SpringDataReservationRepository springDataReservationRepository;
    private final SpringDataResourceRepository springDataResourceRepository;

    public JpaReservationRepository(
            SpringDataReservationRepository springDataReservationRepository,
            SpringDataResourceRepository springDataResourceRepository) {
        if (springDataReservationRepository == null) {
            throw new IllegalArgumentException("springDataReservationRepository must not be null");
        }
        if (springDataResourceRepository == null) {
            throw new IllegalArgumentException("springDataResourceRepository must not be null");
        }
        this.springDataReservationRepository = springDataReservationRepository;
        this.springDataResourceRepository = springDataResourceRepository;
    }

    @Override
    public void save(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        ReservationEntity entity = new ReservationEntity(
                reservation.resource().getId(),
                reservation.timeSlot().start(),
                reservation.timeSlot().end());
        springDataReservationRepository.save(entity);
    }

    @Override
    public List<Reservation> findByResourceId(long resourceId) {
        Optional<ResourceEntity> resourceEntity = springDataResourceRepository.findById(resourceId);
        if (resourceEntity.isEmpty()) {
            return List.of();
        }

        Resource resource = ResourceEntityMapper.toDomain(resourceEntity.get());

        return springDataReservationRepository.findByResourceId(resourceId).stream()
                .map(entity -> new Reservation(
                        resource,
                        new TimeSlot(entity.getStartTime(), entity.getEndTime())))
                .toList();
    }

    @Override
    public List<Reservation> findAll() {
        List<ReservationEntity> reservationEntities = springDataReservationRepository.findAll();

        Set<Long> resourceIds = reservationEntities.stream()
                .map(ReservationEntity::getResourceId)
                .collect(Collectors.toSet());

        Map<Long, Resource> resourcesById = springDataResourceRepository.findAllById(resourceIds).stream()
                .map(ResourceEntityMapper::toDomain)
                .collect(Collectors.toMap(Resource::getId, resource -> resource));

        return reservationEntities.stream()
                .map(entity -> {
                    Resource resource = resourcesById.get(entity.getResourceId());
                    if (resource == null) {
                        throw new IllegalStateException(
                                "no resource found for id " + entity.getResourceId());
                    }
                    return new Reservation(
                            resource,
                            new TimeSlot(entity.getStartTime(), entity.getEndTime()));
                })
                .toList();
    }
}