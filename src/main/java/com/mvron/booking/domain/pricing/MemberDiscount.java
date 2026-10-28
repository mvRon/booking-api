package com.mvron.booking.domain.pricing;

public class MemberDiscount implements DiscountStrategy {

    @Override 
    public long apply(long price, int hours) {
        return price * 90 / 100;
    }
    
}
