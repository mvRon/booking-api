package com.mvron.booking.domain.reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationCalendar {

    private final List<Reservation> reservations = new ArrayList<>();

    public void addReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        for (Reservation existing : reservations) {
            if (existing.conflictsWith(reservation)) {
                throw new IllegalArgumentException("reservation conflicts with existing reservation");
            }
        }
        reservations.add(reservation);
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

}
