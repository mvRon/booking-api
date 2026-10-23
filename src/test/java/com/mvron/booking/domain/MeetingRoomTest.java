package com.mvron.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MeetingRoomTest {

    @Test
    void allowsFourHoursMaximumBooking() {
        MeetingRoom room = new MeetingRoom(1L, "Room A", 10, true);

        assertEquals(4, room.getMaxBookingHours());
    }

    @Test
    void remembersWhetherItHasProjector() {
        MeetingRoom withProjector = new MeetingRoom(1L, "Room A", 10, true);
        MeetingRoom withoutProjector = new MeetingRoom(2L, "Room B", 10, false);

        assertTrue(withProjector.hasProjector());
        assertFalse(withoutProjector.hasProjector());
    }

    @Test
    void passesCommonDataToParent() {
        MeetingRoom room = new MeetingRoom(7L, "Room A", 10, true);

        assertEquals(7L, room.getId());
        assertEquals("Room A", room.getName());
        assertEquals(10, room.getCapacity());
    }

    @Test
    void usesParentValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new MeetingRoom(1L, "", 10, true));
    }

    @Test 
    void hasValidPrice(){
        MeetingRoom meetingRoom = new MeetingRoom(1L, "Room A", 10, true);
        assertEquals(200_000L, meetingRoom.getHourlyPriceVnd());
    }

    @Test 
    void calculateTotalPrice(){
        MeetingRoom meetingRoom = new MeetingRoom(1L, "Room A", 10, true);
        assertEquals( 600_000L, meetingRoom.calculatePrice(3));
    }


}