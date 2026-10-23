package com.mvron.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.Test;

public class EquipmentRentalTest {

    @Test
    void validCommonData() {
        EquipmentRental equipmentRental = new EquipmentRental("projector", 100_000L);
        assertEquals(300_000L, equipmentRental.calculatePrice(3));
        assertEquals("projector", equipmentRental.getEquipmentName());
        assertEquals(100_000L, equipmentRental.getHourlyPriceVnd());
    }

    @Test
    void rejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> new EquipmentRental(null, 100_000L));
    }

    @Test
    void rejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new EquipmentRental(" ", 100_000L));
    }

    @Test
    void rejectZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> new EquipmentRental("projector", 0L));
    }

    @Test
    void rejectUnderZero() {
        assertThrows(IllegalArgumentException.class, () -> new EquipmentRental("projector", -1L));
    }

    @Test
    void acceptsMinimumValidPrice() {
        EquipmentRental rental = new EquipmentRental("Cable", 1L);

        assertEquals(1L, rental.getHourlyPriceVnd());
    }

}
