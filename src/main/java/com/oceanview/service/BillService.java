package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.BillDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BillService {

    private BillDAO billDAO = new BillDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    public String generateBill(int reservationId, int userId) {
        Reservation r = reservationDAO.getReservationById(reservationId);
        if (r == null) return "Reservation not found";
        if (r.getStatus().equals("CANCELLED")) return "Cannot generate bill for a cancelled reservation";

        // Check if bill already exists
        Bill existing = billDAO.getBillByReservationId(reservationId);
        if (existing != null) return "SUCCESS:" + existing.getBillId();

        // Calculate nights and total
        long nights = ChronoUnit.DAYS.between(
                LocalDate.parse(r.getCheckInDate()),
                LocalDate.parse(r.getCheckOutDate())
        );
        if (nights <= 0) return "Invalid reservation dates";

        double total = nights * r.getPricePerNight();

        Bill bill = new Bill();
        bill.setReservationId(reservationId);
        bill.setTotalAmount(total);
        bill.setNumberOfNights((int) nights);
        bill.setPaid(false);
        bill.setGeneratedDate(LocalDate.now().toString());

        int billId = billDAO.createBill(bill);
        if (billId == -1) return "Failed to generate bill";

        auditLogDAO.log(userId, "GENERATE_BILL",
                "Generated bill #" + billId + " for reservation #" + reservationId +
                        " — LKR " + total);
        return "SUCCESS:" + billId;
    }

    public Bill getBillByReservationId(int reservationId) {
        Bill bill = billDAO.getBillByReservationId(reservationId);
        if (bill == null) return null;

        Reservation r = reservationDAO.getReservationById(reservationId);
        if (r != null) {
            bill.setGuestName(r.getGuestName());
            bill.setRoomNumber(r.getRoomNumber());
            bill.setRoomType(r.getRoomType());
            bill.setPricePerNight(r.getPricePerNight());
            bill.setCheckInDate(r.getCheckInDate());
            bill.setCheckOutDate(r.getCheckOutDate());
        }
        return bill;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = billDAO.getAllBills();
        for (Bill bill : bills) {
            Reservation r = reservationDAO.getReservationById(bill.getReservationId());
            if (r != null) {
                bill.setGuestName(r.getGuestName());
                bill.setRoomNumber(r.getRoomNumber());
                bill.setRoomType(r.getRoomType());
                bill.setPricePerNight(r.getPricePerNight());
                bill.setCheckInDate(r.getCheckInDate());
                bill.setCheckOutDate(r.getCheckOutDate());
            }
        }
        return bills;
    }

    public String markAsPaid(int billId, int userId) {
        Bill bill = billDAO.getBillById(billId);
        if (bill == null) return "Bill not found";
        if (bill.isPaid()) return "Bill is already marked as paid";

        // Block if reservation is cancelled
        Reservation r = reservationDAO.getReservationById(bill.getReservationId());
        if (r != null && r.getStatus().equals("CANCELLED")) {
            return "Cannot mark bill as paid for a cancelled reservation";
        }

        boolean success = billDAO.markAsPaid(billId);
        if (success) {
            auditLogDAO.log(userId, "MARK_PAID",
                    "Marked bill #" + billId + " as paid");
            return "SUCCESS";
        }
        return "Failed to mark bill as paid";
    }
}