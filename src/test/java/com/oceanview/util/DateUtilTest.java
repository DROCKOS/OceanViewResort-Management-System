package com.oceanview.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilTest {

    // ── calculateNights ───────────────────────────────────────────────────
    @Test
    void testCalculateNightsFourNights() {
        long nights = DateUtil.calculateNights("2025-06-01", "2025-06-05");
        assertEquals(4, nights);
    }

    @Test
    void testCalculateNightsOneNight() {
        long nights = DateUtil.calculateNights("2025-08-10", "2025-08-11");
        assertEquals(1, nights);
    }

    @Test
    void testCalculateNightsTenNights() {
        long nights = DateUtil.calculateNights("2025-07-01", "2025-07-11");
        assertEquals(10, nights);
    }

    // ── isValidDateRange ──────────────────────────────────────────────────
    @Test
    void testValidDateRange() {
        assertTrue(DateUtil.isValidDateRange("2025-06-01", "2025-06-05"));
    }

    @Test
    void testSameDayRangeIsInvalid() {
        assertFalse(DateUtil.isValidDateRange("2025-06-01", "2025-06-01"));
    }

    @Test
    void testCheckoutBeforeCheckinIsInvalid() {
        assertFalse(DateUtil.isValidDateRange("2025-06-10", "2025-06-05"));
    }

    @Test
    void testInvalidDateFormatReturnsFalse() {
        assertFalse(DateUtil.isValidDateRange("not-a-date", "2025-06-05"));
    }

    // ── isValidDate ───────────────────────────────────────────────────────
    @Test
    void testValidDate() {
        assertTrue(DateUtil.isValidDate("2025-12-31"));
    }

    @Test
    void testInvalidDateFormat() {
        assertFalse(DateUtil.isValidDate("31-12-2025"));
    }

    @Test
    void testGarbageStringIsInvalidDate() {
        assertFalse(DateUtil.isValidDate("hello"));
    }

    // ── isFutureOrToday ───────────────────────────────────────────────────
    @Test
    void testTodayIsValid() {
        String today = LocalDate.now().toString();
        assertTrue(DateUtil.isFutureOrToday(today));
    }

    @Test
    void testFutureDateIsValid() {
        String future = LocalDate.now().plusDays(10).toString();
        assertTrue(DateUtil.isFutureOrToday(future));
    }

    @Test
    void testPastDateIsInvalid() {
        assertFalse(DateUtil.isFutureOrToday("2020-01-01"));
    }

    // ── getCurrentDate ────────────────────────────────────────────────────
    @Test
    void testGetCurrentDateMatchesToday() {
        String today = LocalDate.now().toString();
        assertEquals(today, DateUtil.getCurrentDate());
    }
}
