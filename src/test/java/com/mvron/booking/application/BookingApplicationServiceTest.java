package com.mvron.booking.application;

import com.mvron.booking.domain.MeetingRoom;
import com.mvron.booking.domain.ResourceNotFoundException;
import com.mvron.booking.domain.reservation.Reservation;
import com.mvron.booking.domain.reservation.ReservationConflictException;
import com.mvron.booking.domain.reservation.ReservationService;
import com.mvron.booking.infrastructure.persistence.InMemoryReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingApplicationServiceTest {

    private FakeResourceRepository resourceRepository;
    private ReservationService reservationService;
    private BookingApplicationService bookingService;

    @BeforeEach
    void setUp() {
        resourceRepository = new FakeResourceRepository();
        reservationService = new ReservationService(new InMemoryReservationRepository());
        bookingService = new BookingApplicationService(resourceRepository, reservationService);
    }

    @Test
    void book_returnsReservation_whenResourceExists() {
        resourceRepository.add(new MeetingRoom(1L, "Room A", 10, true));
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 11, 0);

        Reservation result = bookingService.book(1L, start, end);

        assertEquals(1L, result.resource().getId());
        assertEquals(start, result.timeSlot().start());
        assertEquals(1, reservationService.getReservations().size());
    }

    @Test
    void book_throwsNotFound_whenResourceDoesNotExist() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 11, 0);

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.book(999L, start, end));
    }

    @Test
    void book_throwsConflict_whenTimeSlotOverlaps() {
        resourceRepository.add(new MeetingRoom(1L, "Room A", 10, true));
        bookingService.book(1L,
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0));

        assertThrows(ReservationConflictException.class,
                () -> bookingService.book(1L,
                        LocalDateTime.of(2026, 11, 10, 10, 0),
                        LocalDateTime.of(2026, 11, 10, 12, 0)));
    }
}