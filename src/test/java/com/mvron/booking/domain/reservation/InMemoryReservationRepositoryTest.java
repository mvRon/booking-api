package com.mvron.booking.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.mvron.booking.domain.timeslot.TimeSlot;

public class InMemoryReservationRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(DAY.atTime(startHour, 0), DAY.atTime(endHour, 0));
    }

    @Test
    void rejectsNullSave() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void returnsEmptyListWhenNoReservations() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        assertTrue(repository.findAll().isEmpty()); 
    }

    @Test
    void returnsSavedThenFindAllReservations() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        Reservation reservation = new Reservation(new FakeResource(1L, 8), slot(9, 11));
        repository.save(reservation);
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findAll().contains(reservation));
    }

    @Test 
    void returnsFindByResourceIdReservations() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        Reservation reservation1 = new Reservation(new FakeResource(1L, 8), slot(9, 11));
        Reservation reservation2 = new Reservation(new FakeResource(2L, 8), slot(10, 12));
        repository.save(reservation1);
        repository.save(reservation2);
        assertTrue(repository.findByResourceId(1L).contains(reservation1));
        assertEquals(1, repository.findByResourceId(1L).size());
    }

    @Test 
    void returnsEmptyListWhenNoReservationsForResourceId() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        assertTrue(repository.findByResourceId(1L).isEmpty());
    }

    @Test 
    void returnsUnsupportedOperationExceptionWhenFindAll() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        assertThrows(UnsupportedOperationException.class, () -> repository.findAll().add(new Reservation(new FakeResource(1L, 8), slot(9, 11))));
    }

    @Test 
    void returnsSavedTwoDifferentReservationsForSameResourceId() {
        InMemoryReservationRepository repository = new InMemoryReservationRepository();
        Reservation reservation1 = new Reservation(new FakeResource(1L, 8), slot(9, 11));
        Reservation reservation2 = new Reservation(new FakeResource(1L, 8), slot(11, 12));
        repository.save(reservation1);
        repository.save(reservation2);
        assertEquals(2, repository.findByResourceId(1L).size());
    }


}
