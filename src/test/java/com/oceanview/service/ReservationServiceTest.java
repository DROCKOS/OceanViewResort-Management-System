package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock private ReservationDAO reservationDAO;
    @Mock private GuestDAO guestDAO;
    @Mock private RoomDAO roomDAO;
    @Mock private AuditLogDAO auditLogDAO;

    @InjectMocks
    private ReservationService reservationService;

    private String futureCheckIn;
    private String futureCheckOut;
    private Room availableRoom;
    private Reservation confirmedReservation;
    private Reservation cancelledReservation;

    @BeforeEach
    void setUp() {
        futureCheckIn  = LocalDate.now().plusDays(5).toString();
        futureCheckOut = LocalDate.now().plusDays(9).toString();

        availableRoom = new Room(1, "101", "Deluxe", 5000.0,
                true, "Sea view", "35 m²", 1, 2, "");

        confirmedReservation = new Reservation();
        confirmedReservation.setReservationId(1);
        confirmedReservation.setGuestId(1);
        confirmedReservation.setRoomId(1);
        confirmedReservation.setCheckInDate(futureCheckIn);
        confirmedReservation.setCheckOutDate(futureCheckOut);
        confirmedReservation.setStatus("CONFIRMED");
        confirmedReservation.setGuestName("John Silva");

        cancelledReservation = new Reservation();
        cancelledReservation.setReservationId(2);
        cancelledReservation.setStatus("CANCELLED");
        cancelledReservation.setGuestName("Jane Doe");
    }

    @Test
    void testCreateReservationSuccess() {
        when(roomDAO.getAvailableRooms(futureCheckIn, futureCheckOut)).thenReturn(Arrays.asList(availableRoom));
        when(guestDAO.createGuest(any(Guest.class))).thenReturn(1);
        when(reservationDAO.createReservation(any(Reservation.class))).thenReturn(1);
        String result = reservationService.createReservation(
                "John Silva", "123 Main St", "0771234567",
                "john@test.com", 1, futureCheckIn, futureCheckOut, 1);
        assertTrue(result.startsWith("SUCCESS"));
    }

    @Test
    void testCreateReservationPastCheckInBlocked() {
        String result = reservationService.createReservation(
                "John Silva", "123 Main St", "0771234567",
                "john@test.com", 1, "2020-01-01", "2020-01-05", 1);
        assertTrue(result.toLowerCase().contains("past"));
    }

    @Test
    void testCreateReservationCheckoutBeforeCheckinBlocked() {
        String result = reservationService.createReservation(
                "John Silva", "123 Main St", "0771234567",
                "john@test.com", 1, futureCheckOut, futureCheckIn, 1);
        assertTrue(result.toLowerCase().contains("check-out"));
    }

    @Test
    void testCreateReservationSameDayDatesBlocked() {
        String sameDay = LocalDate.now().plusDays(5).toString();
        String result = reservationService.createReservation(
                "John Silva", "123 Main St", "0771234567",
                "john@test.com", 1, sameDay, sameDay, 1);
        assertNotEquals("SUCCESS", result);
    }

    @Test
    void testCreateReservationInvalidContactBlocked() {
        String result = reservationService.createReservation(
                "John Silva", "123 Main St", "ABCDEF",
                "john@test.com", 1, futureCheckIn, futureCheckOut, 1);
        assertTrue(result.toLowerCase().contains("contact"));
    }

    @Test
    void testCreateReservationEmptyGuestNameBlocked() {
        String result = reservationService.createReservation(
                "", "123 Main St", "0771234567",
                "john@test.com", 1, futureCheckIn, futureCheckOut, 1);
        assertTrue(result.toLowerCase().contains("guest"));
    }

    @Test
    void testCreateReservationNullGuestNameBlocked() {
        String result = reservationService.createReservation(
                null, "123 Main St", "0771234567",
                "john@test.com", 1, futureCheckIn, futureCheckOut, 1);
        assertNotEquals("SUCCESS", result);
    }

    @Test
    void testCreateReservationRoomUnavailableBlocked() {
        when(roomDAO.getAvailableRooms(futureCheckIn, futureCheckOut)).thenReturn(List.of());
        String result = reservationService.createReservation(
                "John Silva", "123 Main St", "0771234567",
                "john@test.com", 1, futureCheckIn, futureCheckOut, 1);
        assertTrue(result.toLowerCase().contains("not available"));
    }

    @Test
    void testCreateReservationEmptyAddressBlocked() {
        String result = reservationService.createReservation(
                "John Silva", "", "0771234567",
                "john@test.com", 1, futureCheckIn, futureCheckOut, 1);
        assertTrue(result.toLowerCase().contains("address"));
    }

    @Test
    void testCancelConfirmedReservationSuccess() {
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(reservationDAO.updateReservationStatus(1, "CANCELLED")).thenReturn(true);
        String result = reservationService.cancelReservation(1, 1);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testCancelAlreadyCancelledReservation() {
        when(reservationDAO.getReservationById(2)).thenReturn(cancelledReservation);
        String result = reservationService.cancelReservation(2, 1);
        assertTrue(result.toLowerCase().contains("already cancelled"));
    }

    @Test
    void testCancelNonExistentReservation() {
        when(reservationDAO.getReservationById(99)).thenReturn(null);
        String result = reservationService.cancelReservation(99, 1);
        assertEquals("Reservation not found", result);
    }

    @Test
    void testDeleteReservationSuccess() {
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(reservationDAO.deleteReservation(1)).thenReturn(true);
        String result = reservationService.deleteReservation(1, 1);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testDeleteNonExistentReservation() {
        when(reservationDAO.getReservationById(99)).thenReturn(null);
        String result = reservationService.deleteReservation(99, 1);
        assertEquals("Reservation not found", result);
    }

    @Test
    void testGetReservationReturnsCorrectOne() {
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        Reservation r = reservationService.getReservation(1);
        assertNotNull(r);
        assertEquals("CONFIRMED", r.getStatus());
    }

    @Test
    void testGetReservationNotFound() {
        when(reservationDAO.getReservationById(99)).thenReturn(null);
        assertNull(reservationService.getReservation(99));
    }

    @Test
    void testUpdateCancelledReservationBlocked() {
        when(reservationDAO.getReservationById(2)).thenReturn(cancelledReservation);
        String result = reservationService.updateReservation(2, 1, futureCheckIn, futureCheckOut, 1);
        assertTrue(result.toLowerCase().contains("cancelled"));
    }

    @Test
    void testUpdateNonExistentReservation() {
        when(reservationDAO.getReservationById(99)).thenReturn(null);
        String result = reservationService.updateReservation(99, 1, futureCheckIn, futureCheckOut, 1);
        assertEquals("Reservation not found", result);
    }
}
