package com.mvron.booking.domain.pricing;

public class NoDiscount implements DiscountStrategy {

    @Override 
    public long apply(long price, int hours){
        return price;
    }
    
}
