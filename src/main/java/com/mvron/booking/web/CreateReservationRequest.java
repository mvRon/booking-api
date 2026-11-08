package com.mvron.booking.web;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull Long resourceId,
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end) {
}