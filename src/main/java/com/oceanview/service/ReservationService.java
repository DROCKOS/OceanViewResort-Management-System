package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private GuestDAO guestDAO = new GuestDAO();
    private RoomDAO roomDAO = new RoomDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    public String createReservation(String guestName, String address, String contactNumber,
                                    String email, int roomId, String checkInDate,
                                    String checkOutDate, int userId) {
        // Validate required fields
        if (guestName == null || guestName.trim().isEmpty()) return "Guest name is required";
        if (address == null || address.trim().isEmpty()) return "Address is required";
        if (contactNumber == null || !contactNumber.matches("\\d{10,15}")) return "Invalid contact number";
        if (checkInDate == null || checkOutDate == null) return "Dates are required";

        // Validate dates
        try {
            LocalDate ci = LocalDate.parse(checkInDate);
            LocalDate co = LocalDate.parse(checkOutDate);
            if (!co.isAfter(ci)) return "Check-out must be after check-in";
            if (ci.isBefore(LocalDate.now())) return "Check-in cannot be in the past";
        } catch (Exception e) { return "Invalid date format"; }

        // Check room availability
        List<Room> available = roomDAO.getAvailableRooms(checkInDate, checkOutDate);
        boolean roomAvailable = available.stream().anyMatch(r -> r.getRoomId() == roomId);
        if (!roomAvailable) return "Selected room is not available for these dates";

        // Create guest
        Guest guest = new Guest();
        guest.setName(guestName.trim());
        guest.setAddress(address.trim());
        guest.setContactNumber(contactNumber.trim());
        guest.setEmail(email != null ? email.trim() : "");
        int guestId = guestDAO.createGuest(guest);
        if (guestId == -1) return "Failed to create guest record";

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setGuestId(guestId);
        reservation.setRoomId(roomId);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        int reservationId = reservationDAO.createReservation(reservation);
        if (reservationId == -1) return "Failed to create reservation";

        auditLogDAO.log(userId, "CREATE_RESERVATION",
                "Created reservation #" + reservationId + " for " + guestName);
        return "SUCCESS:" + reservationId;
    }

    public Reservation getReservation(int id) {
        return reservationDAO.getReservationById(id);
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }

    public List<Reservation> getConfirmedReservations() {
        return reservationDAO.getConfirmedReservations();
    }

    public String cancelReservation(int reservationId, int userId) {
        Reservation r = reservationDAO.getReservationById(reservationId);
        if (r == null) return "Reservation not found";
        if (r.getStatus().equals("CANCELLED")) return "Reservation is already cancelled";
        boolean success = reservationDAO.updateReservationStatus(reservationId, "CANCELLED");
        if (success) {
            auditLogDAO.log(userId, "CANCEL_RESERVATION",
                    "Cancelled reservation #" + reservationId);
            return "SUCCESS";
        }
        return "Failed to cancel reservation";
    }

    public String deleteReservation(int reservationId, int userId) {
        Reservation r = reservationDAO.getReservationById(reservationId);
        if (r == null) return "Reservation not found";
        boolean success = reservationDAO.deleteReservation(reservationId);
        if (success) {
            auditLogDAO.log(userId, "DELETE_RESERVATION",
                    "Deleted reservation #" + reservationId + " for " + r.getGuestName());
            return "SUCCESS";
        }
        return "Failed to delete reservation";
    }

    public String updateReservation(int reservationId, int roomId,
                                    String checkInDate, String checkOutDate, int userId) {
        Reservation r = reservationDAO.getReservationById(reservationId);
        if (r == null) return "Reservation not found";
        if (r.getStatus().equals("CANCELLED")) return "Cannot update a cancelled reservation";

        try {
            LocalDate ci = LocalDate.parse(checkInDate);
            LocalDate co = LocalDate.parse(checkOutDate);
            if (!co.isAfter(ci)) return "Check-out must be after check-in";
        } catch (Exception e) { return "Invalid date format"; }

        r.setRoomId(roomId);
        r.setCheckInDate(checkInDate);
        r.setCheckOutDate(checkOutDate);
        boolean success = reservationDAO.updateReservation(r);
        if (success) {
            auditLogDAO.log(userId, "UPDATE_RESERVATION",
                    "Updated reservation #" + reservationId);
            return "SUCCESS";
        }
        return "Failed to update reservation";
    }

    public List<Reservation> getReservationsByStatus(String status) {
        return reservationDAO.getReservationsByStatus(status);
    }
}