package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NoDiscountTest {
    @Test
    void returnsOriginalPrice() {
        DiscountStrategy noDiscount = new NoDiscount();
        assertEquals(100_000L, noDiscount.apply(100_000L, 3));
    }
}
