package com.mvron.booking.domain.pricing;

import com.mvron.booking.domain.Priceable;

public class DiscountedPriceCalculator {
    private final DiscountStrategy strategy;

    public DiscountedPriceCalculator(DiscountStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy can't be null");
        }
        this.strategy = strategy;
    }

    public long calculate(Priceable item, int hours) {
        long originalPrice = item.calculatePrice(hours);
        return strategy.apply(originalPrice, hours);
    }

}
