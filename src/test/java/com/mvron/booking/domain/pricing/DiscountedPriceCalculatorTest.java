package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.mvron.booking.domain.Priceable;

public class DiscountedPriceCalculatorTest {

    private static Priceable item = () -> 25_000L;
    private static DiscountStrategy strategy = (price, hours) -> price / 2;

    @Test
    void rejectsNullStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new DiscountedPriceCalculator(null));
    }

    @Test
    void rejectsZeroHours() {
        assertThrows(IllegalArgumentException.class, () -> new DiscountedPriceCalculator(strategy).calculate(item, 0));
    }

    @Test
    void calculatesValidDataWithFourHours() {
        assertEquals(50_000L, strategy.apply(item.calculatePrice(4), 4));
    }

    @Test
    void calculatesValidDataWithFiveHours() {
        DiscountStrategy longBookingDiscount = new LongBookingDiscount();
        assertEquals(100_000L, longBookingDiscount.apply(item.calculatePrice(5), 5));
    }

}
