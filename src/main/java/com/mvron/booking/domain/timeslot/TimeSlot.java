package com.mvron.booking.domain.timeslot;

import java.time.Duration;
import java.time.LocalDateTime;

public record TimeSlot(LocalDateTime start, LocalDateTime end) {
    public TimeSlot {
        if (start == null) throw new IllegalArgumentException("start must not be null");
        if (end == null) throw new IllegalArgumentException("end must not be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    public boolean overlaps(TimeSlot other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public Duration duration() {
        return Duration.between(start, end);
    }
}