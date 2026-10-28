package com.mvron.booking.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MemberDiscountTest {
    

    @Test 
    void discountTenPercent(){
        DiscountStrategy memberDiscount = new MemberDiscount();
        assertEquals(90_000L, memberDiscount.apply(100_000L, 3));
    }

    @Test 
    void discountDifferentPrice(){
        DiscountStrategy memberDiscount = new MemberDiscount();
        assertEquals(225_000L, memberDiscount.apply(250_000L, 1));
    }

    @Test 
    void discountRounDown(){
        DiscountStrategy memberDiscount = new MemberDiscount();
        assertEquals(89_999L, memberDiscount.apply(99_999L, 1));
    }


}
