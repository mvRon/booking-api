package com.mvron.booking.domain.pricing;

public class LongBookingDiscount implements DiscountStrategy {

    public static final long MIN_HOURS = 5;

    @Override
    public long apply(long price, int hours) {
        
        if (hours < MIN_HOURS) {
            return price;
        }
        return price * 80 / 100;
    }

}
