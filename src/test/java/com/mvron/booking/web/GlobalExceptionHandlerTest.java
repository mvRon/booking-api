package com.mvron.booking.web;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private DataIntegrityViolationException violationOf(String databaseMessage) {
        return new DataIntegrityViolationException(
                "could not execute statement", new RuntimeException(databaseMessage));
    }

    @Test
    void handleDataIntegrity_returns409_whenOverlappingConstraintViolated() {
        ProblemDetail result = handler.handleDataIntegrity(violationOf(
                "ERROR: conflicting key value violates exclusion constraint \"no_overlapping_reservation\""));

        assertEquals(HttpStatus.CONFLICT.value(), result.getStatus());
    }

    @Test
    void handleDataIntegrity_returns400_whenTimeConstraintViolated() {
        ProblemDetail result = handler.handleDataIntegrity(violationOf(
                "ERROR: new row violates check constraint \"chk_reservation_time\""));

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
    }

    @Test
    void handleDataIntegrity_returns404_whenResourceForeignKeyViolated() {
        ProblemDetail result = handler.handleDataIntegrity(violationOf(
                "ERROR: insert violates foreign key constraint \"reservation_resource_id_fkey\""));

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
    }

    @Test
    void handleDataIntegrity_returns500_whenConstraintIsUnknown() {
        ProblemDetail result = handler.handleDataIntegrity(violationOf(
                "ERROR: some other constraint \"unknown_constraint\""));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
    }
}