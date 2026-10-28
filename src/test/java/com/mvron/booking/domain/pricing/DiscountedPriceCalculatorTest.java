package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.mvron.booking.domain.Priceable;

public class DiscountedPriceCalculatorTest {

    private static final Priceable item = () -> 25_000L;
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
    void appliesStrategyToOriginalPrice() {
        DiscountedPriceCalculator calculator = new DiscountedPriceCalculator(strategy);
        assertEquals(50_000L, calculator.calculate(item, 4));
    }

    @Test
    void appliesLongBookingDiscountFromFiveHours() {
        DiscountedPriceCalculator calculator = new DiscountedPriceCalculator(new LongBookingDiscount());
        assertEquals(100_000L, calculator.calculate(item, 5));
    }

    @Test
    void passesOriginalPriceAndHoursToStrategy() {
        DiscountStrategy adder = (price, hours) -> price + hours;
        DiscountedPriceCalculator calculator = new DiscountedPriceCalculator(adder);
        assertEquals(100_004L, calculator.calculate(item, 4)); 
    }

    @Test 
    void intergrationTestWithMemberDiscount() {
        DiscountStrategy memberDiscount = new MemberDiscount();
        DiscountedPriceCalculator calculator = new DiscountedPriceCalculator(memberDiscount);
        assertEquals(90_000L, calculator.calculate(item, 4));
    }

    @Test 
    void discountForMemberIsTrue() {
        boolean isMember = true;
        DiscountStrategy decision = isMember ? new MemberDiscount() : new NoDiscount();
        DiscountedPriceCalculator calculator = new DiscountedPriceCalculator(decision);
        assertEquals(90_000L, calculator.calculate(item, 4));
    }

    @Test 
    void discountForMemberIsFalse() {
        boolean isMember = false;
        DiscountStrategy decision = isMember ? new MemberDiscount() : new NoDiscount();
        DiscountedPriceCalculator calculator = new DiscountedPriceCalculator(decision);
        assertEquals(100_000L, calculator.calculate(item, 4));
    }

}
