package com.mvron.booking.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.mvron.booking.domain.MeetingRoom;
import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.reservation.Reservation;
import com.mvron.booking.domain.reservation.ReservationService;
import com.mvron.booking.domain.timeslot.TimeSlot;

@SpringBootTest
class JpaReservationRepositoryIntegrationTest {

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private SpringDataReservationRepository springDataReservationRepository;

    @Autowired
    private SpringDataResourceRepository springDataResourceRepository;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        springDataReservationRepository.deleteAll();
        springDataResourceRepository.deleteAll();
        service = new ReservationService(reservationRepository);
    }

    private Resource saveMeetingRoom(String name) {
        ResourceEntity saved = springDataResourceRepository.save(
                new ResourceEntity("MEETING_ROOM", name, 10, true));
        return ResourceEntityMapper.toDomain(saved);
    }

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(
                LocalDateTime.of(2026, 12, 1, startHour, 0),
                LocalDateTime.of(2026, 12, 1, endHour, 0));
    }

    @Test
    void savesReservationAndReadsItBack() {
        Resource room = saveMeetingRoom("Room A");

        service.addReservation(new Reservation(room, slot(9, 11)));

        List<Reservation> result = service.getReservations();
        assertEquals(1, result.size());
        assertInstanceOf(MeetingRoom.class, result.get(0).resource());
        assertEquals(room.getId(), result.get(0).resource().getId());
        assertEquals(slot(9, 11), result.get(0).timeSlot());
    }

    @Test
    void findByResourceIdReturnsOnlyReservationsOfThatRoom() {
        Resource roomA = saveMeetingRoom("Room A");
        Resource roomB = saveMeetingRoom("Room B");
        service.addReservation(new Reservation(roomA, slot(9, 11)));
        service.addReservation(new Reservation(roomB, slot(9, 11)));

        List<Reservation> result = reservationRepository.findByResourceId(roomA.getId());

        assertEquals(1, result.size());
        assertEquals(roomA.getId(), result.get(0).resource().getId());
    }

    @Test
    void serviceRejectsOverlappingReservation() {
        Resource room = saveMeetingRoom("Room A");
        service.addReservation(new Reservation(room, slot(9, 11)));

        assertThrows(IllegalArgumentException.class,
                () -> service.addReservation(new Reservation(room, slot(10, 12))));

        assertEquals(1, service.getReservations().size());
    }

    @Test
    void databaseRejectsOverlapEvenWhenServiceIsBypassed() {
        Resource room = saveMeetingRoom("Room A");
        reservationRepository.save(new Reservation(room, slot(9, 11)));

        assertThrows(DataIntegrityViolationException.class,
                () -> reservationRepository.save(new Reservation(room, slot(10, 12))));

        assertEquals(1, reservationRepository.findAll().size());
    }
}