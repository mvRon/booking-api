package com.mvron.booking.domain.pricing;

import java.util.List;

public final class CompositeDiscount implements DiscountStrategy {

    private final List<DiscountStrategy> strategies;

    public CompositeDiscount(List<DiscountStrategy> strategies) {
        if(strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("strategies must not be null or empty");
        }
        this.strategies = List.copyOf(strategies);
    }

    @Override 
    public long apply(long price, int hours) {
        long result = price;
        for (DiscountStrategy strategy : strategies) {
            result = strategy.apply(result, hours);
        }
        return result;
    }
    
}
