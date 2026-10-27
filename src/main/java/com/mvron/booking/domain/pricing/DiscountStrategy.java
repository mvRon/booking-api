package com.mvron.booking.domain.pricing;

public interface DiscountStrategy {
    long apply(long price, int hours);

}
