package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.BillDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillServiceTest {

    @Mock private BillDAO billDAO;
    @Mock private ReservationDAO reservationDAO;
    @Mock private AuditLogDAO auditLogDAO;

    @InjectMocks
    private BillService billService;

    private Reservation confirmedReservation;
    private Reservation cancelledReservation;
    private Bill unpaidBill;
    private Bill paidBill;

    @BeforeEach
    void setUp() {
        String checkIn  = LocalDate.now().plusDays(1).toString();
        String checkOut = LocalDate.now().plusDays(5).toString();

        confirmedReservation = new Reservation();
        confirmedReservation.setReservationId(1);
        confirmedReservation.setStatus("CONFIRMED");
        confirmedReservation.setCheckInDate(checkIn);
        confirmedReservation.setCheckOutDate(checkOut);
        confirmedReservation.setPricePerNight(5000.0);
        confirmedReservation.setGuestName("John Silva");
        confirmedReservation.setRoomNumber("101");
        confirmedReservation.setRoomType("Deluxe");

        cancelledReservation = new Reservation();
        cancelledReservation.setReservationId(2);
        cancelledReservation.setStatus("CANCELLED");
        cancelledReservation.setCheckInDate(checkIn);
        cancelledReservation.setCheckOutDate(checkOut);
        cancelledReservation.setPricePerNight(5000.0);

        unpaidBill = new Bill();
        unpaidBill.setBillId(1);
        unpaidBill.setReservationId(1);
        unpaidBill.setTotalAmount(20000.0);
        unpaidBill.setPaid(false);

        paidBill = new Bill();
        paidBill.setBillId(2);
        paidBill.setReservationId(1);
        paidBill.setTotalAmount(20000.0);
        paidBill.setPaid(true);
    }

    @Test
    void testGenerateBillSuccess() {
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(billDAO.getBillByReservationId(1)).thenReturn(null);
        when(billDAO.createBill(any(Bill.class))).thenReturn(1);
        String result = billService.generateBill(1, 1);
        assertTrue(result.startsWith("SUCCESS"));
    }

    @Test
    void testGenerateBillForCancelledReservationBlocked() {
        when(reservationDAO.getReservationById(2)).thenReturn(cancelledReservation);
        String result = billService.generateBill(2, 1);
        assertTrue(result.toLowerCase().contains("cancelled"));
    }

    @Test
    void testGenerateBillReservationNotFound() {
        when(reservationDAO.getReservationById(99)).thenReturn(null);
        String result = billService.generateBill(99, 1);
        assertEquals("Reservation not found", result);
    }

    @Test
    void testGenerateBillReturnExistingIfAlreadyExists() {
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(billDAO.getBillByReservationId(1)).thenReturn(unpaidBill);
        String result = billService.generateBill(1, 1);
        assertTrue(result.startsWith("SUCCESS"));
    }

    @Test
    void testGenerateBillCalculatesFourNightsCorrectly() {
        String checkIn  = LocalDate.now().plusDays(2).toString();
        String checkOut = LocalDate.now().plusDays(6).toString();
        confirmedReservation.setCheckInDate(checkIn);
        confirmedReservation.setCheckOutDate(checkOut);
        confirmedReservation.setPricePerNight(5000.0);
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(billDAO.getBillByReservationId(1)).thenReturn(null);
        when(billDAO.createBill(argThat(b -> b.getTotalAmount() == 20000.0))).thenReturn(1);
        String result = billService.generateBill(1, 1);
        assertTrue(result.startsWith("SUCCESS"));
    }

    @Test
    void testMarkAsPaidSuccess() {
        when(billDAO.getBillById(1)).thenReturn(unpaidBill);
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(billDAO.markAsPaid(1)).thenReturn(true);
        String result = billService.markAsPaid(1, 1);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testMarkAlreadyPaidBillBlocked() {
        when(billDAO.getBillById(2)).thenReturn(paidBill);
        String result = billService.markAsPaid(2, 1);
        assertTrue(result.toLowerCase().contains("already"));
    }

    @Test
    void testMarkAsPaidBillNotFound() {
        when(billDAO.getBillById(99)).thenReturn(null);
        String result = billService.markAsPaid(99, 1);
        assertEquals("Bill not found", result);
    }

    @Test
    void testMarkAsPaidBlockedForCancelledReservation() {
        unpaidBill.setReservationId(2);
        when(billDAO.getBillById(1)).thenReturn(unpaidBill);
        when(reservationDAO.getReservationById(2)).thenReturn(cancelledReservation);
        String result = billService.markAsPaid(1, 1);
        assertTrue(result.toLowerCase().contains("cancelled"));
    }

    @Test
    void testGetBillByReservationIdReturnsBill() {
        when(billDAO.getBillByReservationId(1)).thenReturn(unpaidBill);
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        Bill result = billService.getBillByReservationId(1);
        assertNotNull(result);
        assertEquals(20000.0, result.getTotalAmount());
    }

    @Test
    void testGetBillByReservationIdNotFound() {
        when(billDAO.getBillByReservationId(99)).thenReturn(null);
        assertNull(billService.getBillByReservationId(99));
    }

    @Test
    void testBillIsUnpaidByDefault() {
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        when(billDAO.getBillByReservationId(1)).thenReturn(null);
        when(billDAO.createBill(any(Bill.class))).thenReturn(1);
        billService.generateBill(1, 1);
        verify(billDAO).createBill(argThat(b -> !b.isPaid()));
    }

    @Test
    void testGetAllBillsReturnsList() {
        Bill b1 = new Bill(); b1.setBillId(1); b1.setReservationId(1);
        Bill b2 = new Bill(); b2.setBillId(2); b2.setReservationId(1);
        when(billDAO.getAllBills()).thenReturn(java.util.Arrays.asList(b1, b2));
        when(reservationDAO.getReservationById(1)).thenReturn(confirmedReservation);
        java.util.List<Bill> bills = billService.getAllBills();
        assertEquals(2, bills.size());
    }

    @Test
    void testGetAllBillsReturnsEmptyList() {
        when(billDAO.getAllBills()).thenReturn(java.util.List.of());
        java.util.List<Bill> bills = billService.getAllBills();
        assertTrue(bills.isEmpty());
    }
}
