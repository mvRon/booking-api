package com.mvron.booking.domain;

public class MeetingRoom extends Resource implements Priceable {
    private static final long HOURLY_PRICE_VND = 200_000;
    private final boolean hasProjector;


    public MeetingRoom(long id, String name, int capacity, boolean hasProjector) {
        super(id, name, capacity);
        this.hasProjector = hasProjector;
    }

    @Override
    public int getMaxBookingHours(){
        return 4;
    }

    public boolean hasProjector(){
        return hasProjector;
    }
    
    @Override 
    public long getHourlyPriceVnd(){
        return HOURLY_PRICE_VND;
    }

}
