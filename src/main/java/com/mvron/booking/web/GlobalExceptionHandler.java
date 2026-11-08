package com.mvron.booking.web;

import com.mvron.booking.domain.ResourceNotFoundException;
import com.mvron.booking.domain.reservation.ReservationConflictException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReservationConflictException.class)
    public ProblemDetail handleConflict(ReservationConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();

        if (cause != null && cause.contains("no_overlapping_reservation")) {
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT, "The resource is already booked for this time slot");
        }
        if (cause != null && cause.contains("chk_reservation_time")) {
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
        if (cause != null && cause.contains("reservation_resource_id_fkey")) {
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.NOT_FOUND, "Resource not found");
        }
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected data integrity error");
    }
}