package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CompositeDiscountTest {

    @Test 
    void rejectsNullList() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeDiscount(null));
    }

    @Test 
    void rejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeDiscount(List.of()));
    }

    @Test
    void applyCombinesLongBookingAndMemberDiscountsSequentially() {
        DiscountStrategy sut = new CompositeDiscount(List.of(
                new LongBookingDiscount(),
                new MemberDiscount()));
        long result = sut.apply(1_000_000, 6);
        assertEquals(720_000L, result);
    }

    @Test 
    void applyCombinesLongBookingUnderFiveHoursAndMemberDiscounts() {
        DiscountStrategy sut = new CompositeDiscount(List.of(
            new MemberDiscount(),
            new LongBookingDiscount()
        ));
        long result = sut.apply(1_000_000, 4);
        assertEquals(900_000L, result);
    }

    @Test 
    void applyLongBookingDiscount() {
        DiscountStrategy sut = new CompositeDiscount(List.of(
            new LongBookingDiscount()
        ));
        long result = sut.apply(500_000, 5);
        assertEquals(400_000L, result);
    }
}
