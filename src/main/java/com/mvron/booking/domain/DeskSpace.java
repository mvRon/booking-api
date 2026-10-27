package com.mvron.booking.domain;

public class DeskSpace extends Resource implements Priceable {

    private static final int SEATS = 1;
    private static final long HOURLY_PRICE_VND = 50_000;

    public DeskSpace(long id, String name) {
        super(id, name, SEATS);
    }

    @Override
    public int getMaxBookingHours() {
        return 8;
    }

    @Override 
    public long getHourlyPriceVnd(){
        return  HOURLY_PRICE_VND;
    }
}