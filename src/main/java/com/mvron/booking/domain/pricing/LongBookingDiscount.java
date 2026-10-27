package com.mvron.booking.domain.pricing;

public class LongBookingDiscount implements DiscountStrategy {

    @Override
    public long apply(long price, int hours) {
        if (hours < 1) {
            throw new IllegalArgumentException("Hours must be greater than or equal to 1");
        }
        if (hours < 5) {
            return price;
        }
        return price * 80 / 100;
    }

}
