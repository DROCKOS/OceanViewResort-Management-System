package com.oceanview.dao;

import com.oceanview.model.Reservation;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public int createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, status, is_deleted) " +
                "VALUES (?, ?, ?, ?, 'CONFIRMED', 0)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getGuestId());
            ps.setInt(2, reservation.getRoomId());
            ps.setString(3, reservation.getCheckInDate());
            ps.setString(4, reservation.getCheckOutDate());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public Reservation getReservationById(int id) {
        String sql = "SELECT r.*, g.name as guest_name, rm.room_number, rm.room_type, rm.price_per_night " +
                "FROM reservations r " +
                "JOIN guests g ON r.guest_id = g.guest_id " +
                "JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.reservation_id = ? AND r.is_deleted = 0";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapReservation(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, g.name as guest_name, rm.room_number, rm.room_type, rm.price_per_night " +
                "FROM reservations r " +
                "JOIN guests g ON r.guest_id = g.guest_id " +
                "JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.is_deleted = 0 ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapReservation(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateReservationStatus(int id, String status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET room_id=?, check_in_date=?, check_out_date=? " +
                "WHERE reservation_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservation.getRoomId());
            ps.setString(2, reservation.getCheckInDate());
            ps.setString(3, reservation.getCheckOutDate());
            ps.setInt(4, reservation.getReservationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteReservation(int id) {
        String sql = "UPDATE reservations SET is_deleted = 1 WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Reservation> getReservationsByStatus(String status) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, g.name as guest_name, rm.room_number, rm.room_type, rm.price_per_night " +
                "FROM reservations r " +
                "JOIN guests g ON r.guest_id = g.guest_id " +
                "JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.status = ? AND r.is_deleted = 0 ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapReservation(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // For calendar — get all confirmed reservations with dates
    public List<Reservation> getConfirmedReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, g.name as guest_name, rm.room_number, rm.room_type, rm.price_per_night " +
                "FROM reservations r " +
                "JOIN guests g ON r.guest_id = g.guest_id " +
                "JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.status = 'CONFIRMED' AND r.is_deleted = 0 ORDER BY r.check_in_date ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapReservation(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setGuestId(rs.getInt("guest_id"));
        r.setRoomId(rs.getInt("room_id"));
        r.setCheckInDate(rs.getString("check_in_date"));
        r.setCheckOutDate(rs.getString("check_out_date"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getString("created_at"));
        r.setGuestName(rs.getString("guest_name"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(rs.getString("room_type"));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        return r;
    }
}