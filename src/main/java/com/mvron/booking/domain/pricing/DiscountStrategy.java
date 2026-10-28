package com.mvron.booking.domain.pricing;

public interface DiscountStrategy {

     /**
     * @param hours must be at least 1; the caller is responsible for validating it
     */

    long apply(long price, int hours);

}
