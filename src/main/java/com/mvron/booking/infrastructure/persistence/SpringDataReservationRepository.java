package com.mvron.booking.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, Long> {
       List<ReservationEntity> findByResourceId(Long resourceId);
}
