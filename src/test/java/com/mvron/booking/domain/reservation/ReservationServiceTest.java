package com.mvron.booking.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

// import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.timeslot.TimeSlot;

public class ReservationServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(DAY.atTime(startHour, 0), DAY.atTime(endHour, 0));
    }

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

    @Test
    void addReservation_acceptsWhenCalendarIsEmpty() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(9, 11)));
        assertEquals(1, calendar.getReservations().size());
    }

    @Test
    void addReservation_acceptsWhenSameResourceAndNonOverlappingTimeSlots() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(9, 11)));
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(11, 12)));
        assertEquals(2, calendar.getReservations().size());
    }

    @Test
    void addReservation_acceptsWhenDifferentResourceAndOverlappingTimeSlots() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(9, 11)));
        calendar.addReservation(new Reservation(new FakeResource(2, 8), slot(10, 12)));
        assertEquals(2, calendar.getReservations().size());
    }

    @Test
    void addReservation_rejectsWhenSameResourceAndOverlappingTimeSlots() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(9, 11)));
        assertThrows(IllegalArgumentException.class,
                () -> calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(10, 12))));
    }

    @Test
    void addReservation_rejectsNullReservation() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(9, 11)));
        assertThrows(IllegalArgumentException.class, () -> calendar.addReservation(null));
    }

    @Test
    void getReservations_returnsImmutableList() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(9, 11)));

        var reservations = calendar.getReservations();

        assertThrows(UnsupportedOperationException.class, reservations::clear);
        assertEquals(1, calendar.getReservations().size());
    }

    @Test
    void addReservation_leavesCalendarUnchanged_whenRejected() {
        ReservationService calendar = new ReservationService(new InMemoryReservationRepository());
        Reservation first = new Reservation(new FakeResource(1, 8), slot(9, 11));
        calendar.addReservation(first);

        assertThrows(IllegalArgumentException.class,
                () -> calendar.addReservation(new Reservation(new FakeResource(1, 8), slot(10, 12))));

        assertEquals(List.of(first), calendar.getReservations());
    }

}
