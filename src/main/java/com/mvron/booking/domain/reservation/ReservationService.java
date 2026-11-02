package com.mvron.booking.domain.reservation;

import java.util.List;

public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        if (reservationRepository == null) {
            throw new IllegalArgumentException("reservationRepository must not be null");
        }
        this.reservationRepository = reservationRepository;
    }


    public void addReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not be null");
        }
        List<Reservation> existingReservations = reservationRepository.findByResourceId(reservation.resource().getId());
        for (Reservation existingReservation : existingReservations) {
            if (reservation.conflictsWith(existingReservation)) {
                throw new IllegalArgumentException("reservation conflicts with existing reservation");
            }
        }
        reservationRepository.save(reservation);
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

}
