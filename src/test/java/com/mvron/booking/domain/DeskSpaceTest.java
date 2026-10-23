package com.mvron.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DeskSpaceTest {

    @Test
    void allowsEightHoursMaximumBooking() {
        DeskSpace desk = new DeskSpace(1L, "Desk 1");

        assertEquals(8, desk.getMaxBookingHours());
    }

    @Test
    void alwaysHasCapacityOfOne() {
        DeskSpace desk = new DeskSpace(1L, "Desk 1");

        assertEquals(1, desk.getCapacity());
    }

    @Test
    void passesIdAndNameToParent() {
        DeskSpace desk = new DeskSpace(42L, "Desk 2");

        assertEquals(42L, desk.getId());
        assertEquals("Desk 2", desk.getName());
    }

    @Test
    void usesParentValidationForNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeskSpace(1L, null));
    }

    @Test
    void hasValidPrice() {
        DeskSpace deskspace = new DeskSpace(42L, "Desk 1");
        assertEquals(50_000L, deskspace.getHourlyPriceVnd());
    }

    @Test
    void calculateTotalPrice() {
        DeskSpace deskspace = new DeskSpace(42L, "Desk 1");
        assertEquals(100_000L, deskspace.calculatePrice(2));
    }
}
