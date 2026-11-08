package com.mvron.booking.web;

import com.mvron.booking.application.BookingApplicationService;
import com.mvron.booking.domain.reservation.Reservation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final BookingApplicationService bookingService;

    public ReservationController(BookingApplicationService bookingService) {
        if (bookingService == null) {
            throw new IllegalArgumentException("bookingService must not be null");
        }
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = bookingService.book(
                request.resourceId(), request.start(), request.end());
        return ReservationResponse.from(reservation);
    }
}