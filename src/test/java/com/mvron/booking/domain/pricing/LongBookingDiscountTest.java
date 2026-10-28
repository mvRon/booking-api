package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LongBookingDiscountTest {

    private final DiscountStrategy longBookingDiscount = new LongBookingDiscount();

    @Test
    void keepsPriceAtFourHours() {
        assertEquals(100_000L, longBookingDiscount.apply(100_000, 4));
    }

    @Test
    void appliesTwentyPercentAtFiveHours() {
        assertEquals(80_000L, longBookingDiscount.apply(100_000, 5));
    }

    @Test
    void appliesTwentyPercentAboveFiveHours() {
        assertEquals(80_000L, longBookingDiscount.apply(100_000, 8));
    }

    @Test
    void appliesDiscountToDifferentPrice() {
        assertEquals(200_000L, longBookingDiscount.apply(250_000, 6));
    }

    @Test 
    void keepsPriceAtOneHour() {
        assertEquals(100_000L, longBookingDiscount.apply(100_000L, 1));
    }

}
