package com.mvron.booking.domain.reservation;

import java.util.List;

class SpyReservationRepository implements ReservationRepository {
    private final List<Reservation> existing;
    int saveCallCount = 0;

    SpyReservationRepository(List<Reservation> existing) {
        this.existing = existing;
    }

    // save: saveCallCount++
    @Override
    public void save(Reservation reservation) {
        saveCallCount++;
    }

    // findByResourceId: trả existing (bạn tự quyết có cần lọc theo id không)

    @Override
    public List<Reservation> findByResourceId(long resourceId) {
        return existing;
    }

    // findAll: trả existing
    @Override
    public List<Reservation> findAll() {
        return existing;
    }
}