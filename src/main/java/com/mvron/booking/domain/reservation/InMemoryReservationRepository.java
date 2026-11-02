package com.mvron.booking.domain.reservation;

import java.util.ArrayList;
import java.util.List;


public class InMemoryReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public void save(Reservation reservation) {
        if(reservation == null) {
            throw new IllegalArgumentException("reservation can't be null");
        }
        reservations.add(reservation);
    }

    @Override
    public List<Reservation> findByResourceId(long resourceId) {
        return reservations.stream()
                .filter(reservation -> reservation.resource().getId() == resourceId)
                .toList();
    }


    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }
    
}
