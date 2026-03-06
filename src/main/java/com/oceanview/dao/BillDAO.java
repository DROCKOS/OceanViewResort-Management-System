package com.oceanview.dao;

import com.oceanview.model.Bill;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public int createBill(Bill bill) {
        String sql = "INSERT INTO bills (reservation_id, total_amount, number_of_nights, " +
                "is_paid, generated_date) VALUES (?, ?, ?, false, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bill.getReservationId());
            ps.setDouble(2, bill.getTotalAmount());
            ps.setInt(3, bill.getNumberOfNights());
            ps.setString(4, bill.getGeneratedDate());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public Bill getBillById(int billId) {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBill(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Bill getBillByReservationId(int reservationId) {
        String sql = "SELECT * FROM bills WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBill(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY generated_date DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBill(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean markAsPaid(int billId) {
        String sql = "UPDATE bills SET is_paid = true WHERE bill_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setReservationId(rs.getInt("reservation_id"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setNumberOfNights(rs.getInt("number_of_nights"));
        b.setPaid(rs.getBoolean("is_paid"));
        b.setGeneratedDate(rs.getString("generated_date"));
        return b;
    }
}