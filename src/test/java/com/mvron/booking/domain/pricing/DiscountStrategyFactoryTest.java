package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

public class DiscountStrategyFactoryTest {
    
    @Test 
    void createReturnNoDiscountWhenNotMemberAndShortBooking() {
        DiscountStrategy result = DiscountStrategyFactory.create(false, 4);
        assertInstanceOf(NoDiscount.class, result);
    }

    @Test 
    void createReturnLongBookingDiscountWhenNotMemberAndLongBooking() {
        DiscountStrategy result = DiscountStrategyFactory.create(false, 5);
        assertInstanceOf(LongBookingDiscount.class, result);
    }

    @Test 
    void createReturnMemberDiscountWhenMemberAndShortBooking() {
        DiscountStrategy result = DiscountStrategyFactory.create(true, 4);
        assertInstanceOf(MemberDiscount.class, result);
    }

    @Test 
    void createReturnCompositeDiscountWhenMemberAndLongBooking() {
        DiscountStrategy result = DiscountStrategyFactory.create(true, 5);
        assertInstanceOf(CompositeDiscount.class, result);
    }


    @Test 
    void createReturnNoDiscountWhenNotMemberAndShortBookingWithOneHour() {
        DiscountStrategy result = DiscountStrategyFactory.create(false, 1);
        assertInstanceOf(NoDiscount.class, result);
    }

    @Test 
    void createReturnLongBookingDiscountWhenNotMemberAndLongBookingWithEightHours() {
        DiscountStrategy result = DiscountStrategyFactory.create(false, 8);
        assertInstanceOf(LongBookingDiscount.class, result);
    }

    @Test 
    void createReturnCompositeDiscountWhenMemberAndLongBookingWithEightHours() {
        DiscountStrategy result = DiscountStrategyFactory.create(true, 8);
        assertInstanceOf(CompositeDiscount.class, result);
    }
}
