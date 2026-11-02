package com.mvron.booking.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.timeslot.TimeSlot;

public class ReservationTest {

    // private static class FakeResource extends Resource {
    //     private final int maxHours;

    //     FakeResource(long id, int maxHours) {
    //         super(id, "fake", 1);
    //         this.maxHours = maxHours;
    //     }

    //     @Override
    //     public int getMaxBookingHours() {
    //         return maxHours;
    //     }
    // }

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(DAY.atTime(startHour, 0), DAY.atTime(endHour, 0));
    }

    @Test
    void conflictsWith_returnsTrue_whenDifferentObjectsHaveSameId() {
        Reservation a = new Reservation(new FakeResource(1L, 8), slot(9, 11));
        Reservation b = new Reservation(new FakeResource(1L, 8), slot(10, 12));

        assertTrue(a.conflictsWith(b));
        assertTrue(b.conflictsWith(a));
    }

    @Test
    void rejectsNullResource() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(null, slot(9, 11)));
    }

    @Test
    void rejectsNullTimeSlot() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(new FakeResource(1, 8), null));
    }

    @Test
    void acceptValidReservation() {
        assertDoesNotThrow(() -> new Reservation(new FakeResource(1, 4), slot(9, 11)));
    }

    @Test
    void rejectsReservationExceedingMaxBookingHours() {
        assertThrows(IllegalArgumentException.class, () -> new Reservation(new FakeResource(1, 1), slot(9, 11)));
    }

    @Test
    void rejectsReservationExceedingMaxBookingHoursByOneMinute() {
        assertThrows(IllegalArgumentException.class,
                () -> new Reservation(new FakeResource(1, 2), new TimeSlot(DAY.atTime(9, 0), DAY.atTime(11, 1))));
    }

    @Test
    void conflictWith_returnsTrue_whenSameResourceAndOverlappingTimeSlots() {
        Resource resource = new FakeResource(1, 8);
        Reservation a = new Reservation(resource, slot(9, 11));
        Reservation b = new Reservation(resource, slot(10, 12));

        assertTrue(a.conflictsWith(b));
        assertTrue(b.conflictsWith(a));
    }

    @Test
    void conflictWith_returnsFalse_whenSameResourceAndNonOverlappingTimeSlots() {
        Resource resource = new FakeResource(1, 8);
        Reservation a = new Reservation(resource, slot(9, 11));
        Reservation b = new Reservation(resource, slot(11, 12));

        assertFalse(a.conflictsWith(b));
        assertFalse(b.conflictsWith(a));
    }

    @Test
    void conflictWith_returnsFalse_whenDifferentResourceAndOverlappingTimeSlots() {
        Reservation a = new Reservation(new FakeResource(1, 8), slot(9, 11));
        Reservation b = new Reservation(new FakeResource(2, 8), slot(10, 12));

        assertFalse(a.conflictsWith(b));
        assertFalse(b.conflictsWith(a));
    }

    @Test
    void conflictWith_throwsException_whenOtherIsNull() {
        Reservation a = new Reservation(new FakeResource(1, 8), slot(9, 11));

        assertThrows(IllegalArgumentException.class, () -> a.conflictsWith(null));
    }

    @Test 
    void conflictWith_returnsFalse_whenSameResourceAndAdjacentTimeSlots() {
        Resource resource = new FakeResource(1, 8);
        Reservation a = new Reservation(resource, slot(9, 11));
        Reservation b = new Reservation(resource, slot(11, 12));

        assertFalse(a.conflictsWith(b));
        assertFalse(b.conflictsWith(a));
    }

}
