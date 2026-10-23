package com.mvron.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PriceCalculatorTest {

    @Test
    void totalListPrice() {
        MeetingRoom meetingRoom = new MeetingRoom(1L, "Room A", 2, true);
        DeskSpace deskSpace = new DeskSpace(2L, "Desk 1");
        EquipmentRental equipmentRental = new EquipmentRental("projector", 30_000);
        assertEquals(840_000L, PriceCalculator.totalPrice(List.of(meetingRoom, deskSpace, equipmentRental), 3));
    }

    @Test
    void emptyListEqualsZero() {
        assertEquals(0L, PriceCalculator.totalPrice(List.of(), 3));
    }

    @Test
    void givesSameResultWhenCalledTwice() {
        List<Priceable> items = List.of(new EquipmentRental("Projector", 30_000));

        long first = PriceCalculator.totalPrice(items, 2);
        long second = PriceCalculator.totalPrice(items, 2);

        assertEquals(60_000L, first);
        assertEquals(60_000L, second);
    }

}
