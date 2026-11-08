package com.mvron.booking.application;

import java.time.LocalDateTime;

import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.ResourceNotFoundException;
import com.mvron.booking.domain.ResourceRepository;
import com.mvron.booking.domain.reservation.Reservation;
import com.mvron.booking.domain.reservation.ReservationService;
import com.mvron.booking.domain.timeslot.TimeSlot;

public class BookingApplicationService {

    private final ResourceRepository resourceRepository;
    private final ReservationService reservationService;

    public BookingApplicationService(ResourceRepository resourceRepository,
                                     ReservationService reservationService) {
        if (resourceRepository == null) {
            throw new IllegalArgumentException("resourceRepository must not be null");
        }
        if (reservationService == null) {
            throw new IllegalArgumentException("reservationService must not be null");
        }
        this.resourceRepository = resourceRepository;
        this.reservationService = reservationService;
    }

    public Reservation book(long resourceId, LocalDateTime start, LocalDateTime end) {

        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException(resourceId));

        Reservation reservation = new Reservation(resource, new TimeSlot(start, end));
        reservationService.addReservation(reservation);
        return reservation;

    }


}