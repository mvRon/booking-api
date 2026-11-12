package com.mvron.booking.web;

import com.mvron.booking.application.BookingApplicationService;
import com.mvron.booking.domain.reservation.Reservation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final BookingApplicationService bookingService;

    public ReservationController(BookingApplicationService bookingService) {
        if (bookingService == null) {
            throw new IllegalArgumentException("bookingService must not be null");
        }
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a reservation for a resource")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "409", description = "Resource already booked for this time slot")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = bookingService.book(
                request.resourceId(), request.start(), request.end());
        return ReservationResponse.from(reservation);
    }


    @Operation (summary = "Get reservations for a resource")
    @ApiResponses ({
            @ApiResponse (responseCode = "200", description = "Reservations retrieved"),
            @ApiResponse (responseCode = "404", description = "Resource not found")
    })
    @GetMapping(params = "resourceId")
    @ResponseStatus (HttpStatus.OK)
    public List<ReservationResponse> getReservationsByResourceId(long resourceId) {
        List<Reservation> reservations = bookingService.getReservationsByResourceId(resourceId);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
        
    }


    @Operation (summary = "Get all reservation")
    @ApiResponses ({
            @ApiResponse (responseCode = "200", description = "Reservations retrieved"),
    })
    @GetMapping 
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = bookingService.getReservations();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
    

}