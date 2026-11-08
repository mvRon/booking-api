package com.mvron.booking.web;

import com.mvron.booking.domain.reservation.Reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        long resourceId,
        String resourceName,
        LocalDateTime start,
        LocalDateTime end) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.resource().getId(),
                reservation.resource().getName(),
                reservation.timeSlot().start(),
                reservation.timeSlot().end());
    }
}