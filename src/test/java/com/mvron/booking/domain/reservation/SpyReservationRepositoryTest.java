package com.mvron.booking.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mvron.booking.domain.timeslot.TimeSlot;

public class SpyReservationRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(DAY.atTime(startHour, 0), DAY.atTime(endHour, 0));
    }

    @Test
    void doesNotSaveWhenReservationConflicts() {
        Reservation existing = new Reservation(new FakeResource(1L, 8), slot(9, 11));
        SpyReservationRepository spy = new SpyReservationRepository(List.of(existing));
        ReservationService service = new ReservationService(spy); 
        Reservation conflicting = new Reservation(new FakeResource(1L, 8), slot(10, 12));
       
        assertThrows(ReservationConflictException.class, () -> service.addReservation(conflicting));
        assertEquals(0, spy.saveCallCount);
    }

    @Test
    void callSaveOnceWhenReservationDoesNotConflict() {
        Reservation existing = new Reservation(new FakeResource(1L, 8), slot(9, 11));
        SpyReservationRepository spy = new SpyReservationRepository(List.of(existing));
        ReservationService service = new ReservationService(spy); 
        Reservation nonConflicting = new Reservation(new FakeResource(1L, 8), slot(11, 12));
       
        service.addReservation(nonConflicting);
        assertEquals(1, spy.saveCallCount);
    }
}
