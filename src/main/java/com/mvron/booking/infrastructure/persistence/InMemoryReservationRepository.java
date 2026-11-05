package com.mvron.booking.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;

import com.mvron.booking.domain.reservation.Reservation;
import com.mvron.booking.domain.reservation.ReservationRepository;


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
