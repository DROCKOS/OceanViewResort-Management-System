package com.oceanview.dao;

import com.oceanview.model.Guest;
import com.oceanview.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAO {

    private Connection connection;

    public GuestDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public int createGuest(Guest guest) {
        String sql = "INSERT INTO guests (name, address, contact_number, email) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getAddress());
            stmt.setString(3, guest.getContactNumber());
            stmt.setString(4, guest.getEmail());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error creating guest: " + e.getMessage());
        }
        return -1;
    }

    public Guest getGuestById(int guestId) {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapGuest(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error getting guest: " + e.getMessage());
        }
        return null;
    }

    public List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM guests ORDER BY created_at DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                guests.add(mapGuest(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error getting guests: " + e.getMessage());
        }
        return guests;
    }

    public boolean updateGuest(Guest guest) {
        String sql = "UPDATE guests SET name=?, address=?, contact_number=?, email=? WHERE guest_id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getAddress());
            stmt.setString(3, guest.getContactNumber());
            stmt.setString(4, guest.getEmail());
            stmt.setInt(5, guest.getGuestId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating guest: " + e.getMessage());
        }
        return false;
    }

    private Guest mapGuest(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setGuestId(rs.getInt("guest_id"));
        guest.setName(rs.getString("name"));
        guest.setAddress(rs.getString("address"));
        guest.setContactNumber(rs.getString("contact_number"));
        guest.setEmail(rs.getString("email"));
        guest.setCreatedAt(rs.getString("created_at"));
        return guest;
    }
}