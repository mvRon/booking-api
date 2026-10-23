package com.mvron.booking.domain;

public interface Priceable {
    long getHourlyPriceVnd();

    default long calculatePrice(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than 0");
        }
        return hours * getHourlyPriceVnd();
    }
}
