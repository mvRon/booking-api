package com.mvron.booking.domain.pricing;

public class LongBookingDiscount implements DiscountStrategy {

    @Override
    public long apply(long price, int hours) {
        
        if (hours < 5) {
            return price;
        }
        return price * 80 / 100;
    }

}
