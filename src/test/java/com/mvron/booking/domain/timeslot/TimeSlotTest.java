package com.mvron.booking.domain.timeslot;


import org.junit.jupiter.api.Test;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSlotTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    // ---------- Hàm hỗ trợ ----------

    private static LocalDateTime at(int hour, int minute) {
        return DAY.atTime(hour, minute);
    }

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(at(startHour, 0), at(endHour, 0));
    }

    // Luôn kiểm tra cả hai chiều: vừa test kết quả, vừa test tính đối xứng
    private static void assertOverlap(TimeSlot a, TimeSlot b) {
        assertTrue(a.overlaps(b), "a should overlap b");
        assertTrue(b.overlaps(a), "b should overlap a");
    }

    private static void assertNoOverlap(TimeSlot a, TimeSlot b) {
        assertFalse(a.overlaps(b), "a should not overlap b");
        assertFalse(b.overlaps(a), "b should not overlap a");
    }

    // ---------- overlaps: KHÔNG chồng lấn ----------

    @Test
    void overlaps_returnsFalse_whenFirstSlotIsCompletelyBeforeSecond() {
        assertNoOverlap(slot(9, 10), slot(12, 13));
    }

    @Test
    void overlaps_returnsFalse_whenSlotsAreAdjacent() {
        // Giá trị biên quan trọng nhất: 9-11 rồi 11-13 là hợp lệ
        assertNoOverlap(slot(9, 11), slot(11, 13));
    }

    // ---------- overlaps: CÓ chồng lấn ----------

    @Test
    void overlaps_returnsTrue_whenSlotsPartiallyOverlap() {
        assertOverlap(slot(9, 11), slot(10, 12));
    }

    @Test
    void overlaps_returnsTrue_whenOneSlotContainsTheOther() {
        // Helper kiểm tra cả hai chiều nên cả "A chứa B" lẫn "B chứa A" đều được phủ
        assertOverlap(slot(9, 13), slot(10, 11));
    }

    @Test
    void overlaps_returnsTrue_whenSlotsAreIdentical() {
        assertOverlap(slot(9, 11), slot(9, 11));
    }

    @Test
    void overlaps_returnsTrue_whenSlotsOverlapByOneMinute() {
        // Biên "lệch 1 phút": 10:59 < 11:00 nên phải chồng lấn
        TimeSlot first = slot(9, 11);
        TimeSlot second = new TimeSlot(at(10, 59), at(12, 0));

        assertOverlap(first, second);
    }

    @Test
    void overlaps_returnsFalse_whenSameHoursButDifferentDays() {
        // Chứng minh lý do chọn LocalDateTime thay vì LocalTime
        TimeSlot day2 = new TimeSlot(DAY.atTime(10, 0), DAY.atTime(12, 0));
        TimeSlot day3 = new TimeSlot(DAY.plusDays(1).atTime(10, 0),
                DAY.plusDays(1).atTime(12, 0));

        assertNoOverlap(day2, day3);
    }

    // ---------- Constructor: invariant ----------

    @Test
    void constructor_throwsException_whenStartEqualsEnd() {
        assertThrows(IllegalArgumentException.class,
                () -> new TimeSlot(at(9, 0), at(9, 0)));
    }

    @Test
    void constructor_throwsException_whenStartIsAfterEnd() {
        assertThrows(IllegalArgumentException.class,
                () -> new TimeSlot(at(11, 0), at(9, 0)));
    }

    @Test
    void constructor_throwsException_whenStartIsNull() {
        assertThrows(NullPointerException.class,
                () -> new TimeSlot(null, at(11, 0)));
    }

    @Test
    void constructor_throwsException_whenEndIsNull() {
        assertThrows(NullPointerException.class,
                () -> new TimeSlot(at(9, 0), null));
    }

    // ---------- overlaps(null) ----------

    @Test
    void overlaps_throwsException_whenOtherIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> slot(9, 11).overlaps(null));
    }

    // ---------- duration ----------

    @Test
    void duration_returnsTwoHours_forNineToEleven() {
        assertEquals(Duration.ofHours(2), slot(9, 11).duration());
    }

    @Test
    void duration_returnsNinetyMinutes_whenEndHasMinutes() {
        TimeSlot slot = new TimeSlot(at(9, 0), at(10, 30));

        assertEquals(Duration.ofMinutes(90), slot.duration());
    }
}