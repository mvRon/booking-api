package com.mvron.booking.domain;

public class DeskSpace extends Resource {

    private static final int SEATS = 1;

    public DeskSpace(long id, String name) {
        super(id, name, SEATS);
    }

    @Override
    public int getMaxBookingHours() {
        return 8;
    }
}