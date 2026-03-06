package com.oceanview.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static long calculateNights(String checkInDate, String checkOutDate) {
        LocalDate checkIn = LocalDate.parse(checkInDate, FORMATTER);
        LocalDate checkOut = LocalDate.parse(checkOutDate, FORMATTER);
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public static boolean isValidDateRange(String checkInDate, String checkOutDate) {
        try {
            LocalDate checkIn = LocalDate.parse(checkInDate, FORMATTER);
            LocalDate checkOut = LocalDate.parse(checkOutDate, FORMATTER);
            return checkOut.isAfter(checkIn);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isFutureOrToday(String date) {
        try {
            LocalDate parsed = LocalDate.parse(date, FORMATTER);
            return !parsed.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCurrentDate() {
        return LocalDate.now().format(FORMATTER);
    }
}