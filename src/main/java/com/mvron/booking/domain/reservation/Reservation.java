package com.mvron.booking.domain.reservation;

import java.time.Duration;

import com.mvron.booking.domain.Resource;
import com.mvron.booking.domain.timeslot.TimeSlot;

public record Reservation (Resource resource, TimeSlot timeSlot) {

    public Reservation {
        if (resource == null) throw new IllegalArgumentException("resource must not be null");
        if (timeSlot == null) throw new IllegalArgumentException("timeSlot must not be null");
        if (timeSlot.duration().compareTo(Duration.ofHours(resource.getMaxBookingHours())) > 0) {
            throw new IllegalArgumentException("timeSlot duration must not exceed resource max booking hours");
        }
    }

    public boolean conflictsWith(Reservation other) {
        if (other == null) throw new IllegalArgumentException("other must not be null");
        return resource.getId() == other.resource.getId() && timeSlot.overlaps(other.timeSlot);
    }
    
}
