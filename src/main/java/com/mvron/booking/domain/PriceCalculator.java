package com.mvron.booking.domain;

import java.util.List;

public class PriceCalculator {

    private static long total;
    

    public static long totalPrice(List<Priceable> items, int hours){
        total = 0;
        for(Priceable item : items) {
            total += item.calculatePrice(hours);
        }
        return total;
    }


}
