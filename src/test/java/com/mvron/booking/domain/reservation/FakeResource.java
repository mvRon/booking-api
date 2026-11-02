package com.mvron.booking.domain.reservation;

import com.mvron.booking.domain.Resource;

public class FakeResource extends Resource {
    private final int maxHours;

    FakeResource(long id, int maxHours) {
        super(id, "fake", 1);
        this.maxHours = maxHours;
    }

    @Override
    public int getMaxBookingHours() {
        return maxHours;
    }
}
