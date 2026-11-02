package com.mvron.booking.domain.reservation;

import java.util.List;

public interface ReservationRepository {
    
    void save(Reservation reservation);

    List<Reservation> findByResourceId(long resourceId);

    List<Reservation> findAll();
}
