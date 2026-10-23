package com.mvron.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PriceableTest {

    // A fake Priceable: a lambda that always returns 25_000
    private final Priceable fake = () -> 25_000L;

    @Test
    void multipliesHourlyPriceByHours() {
        assertEquals(100_000L, fake.calculatePrice(4));
    }

    @Test
    void rejectsZeroHours() {
        assertThrows(IllegalArgumentException.class,
                () -> fake.calculatePrice(0));
    }

    @Test
    void rejectsNegativeHours() {
        assertThrows(IllegalArgumentException.class,
                () -> fake.calculatePrice(-1));
    }

    @Test
    void acceptsPositiveHours() {
        assertEquals(25_000L, fake.calculatePrice(1));
    }
}