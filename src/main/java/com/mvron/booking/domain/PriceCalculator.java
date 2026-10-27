package com.mvron.booking.domain;

import java.util.List;

public class PriceCalculator {

    

    public static long totalPrice(List<Priceable> items, int hours){
        long total = 0;
        for(Priceable item : items) {
            total += item.calculatePrice(hours);
        }
        return total;
    }


}
