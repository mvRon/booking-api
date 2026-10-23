package com.mvron.booking.domain;

public class MeetingRoom extends Resource {

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
    
}
