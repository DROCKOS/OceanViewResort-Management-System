package com.oceanview.dao;

import com.oceanview.model.Room;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rooms.add(mapRoom(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }

    public Room getRoomById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRoom(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Room> getAvailableRooms(String checkIn, String checkOut) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE is_available = true AND room_id NOT IN (" +
                "SELECT room_id FROM reservations WHERE status = 'CONFIRMED' AND is_deleted = 0 " +
                "AND NOT (check_out_date <= ? OR check_in_date >= ?)) ORDER BY room_number";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, checkIn);
            ps.setString(2, checkOut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapRoom(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return rooms;
    }

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, is_available, " +
                "description, room_size, num_beds, max_persons, image_url) VALUES (?,?,?,true,?,?,?,?,?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setDouble(3, room.getPricePerNight());
            ps.setString(4, room.getDescription());
            ps.setString(5, room.getRoomSize());
            ps.setInt(6, room.getNumBeds());
            ps.setInt(7, room.getMaxPersons());
            ps.setString(8, room.getImageUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_type=?, price_per_night=?, is_available=?, " +
                "description=?, room_size=?, num_beds=?, max_persons=?, image_url=? WHERE room_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomType());
            ps.setDouble(2, room.getPricePerNight());
            ps.setBoolean(3, room.isAvailable());
            ps.setString(4, room.getDescription());
            ps.setString(5, room.getRoomSize());
            ps.setInt(6, room.getNumBeds());
            ps.setInt(7, room.getMaxPersons());
            ps.setString(8, room.getImageUrl());
            ps.setInt(9, room.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public Room mapRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(rs.getString("room_type"));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        r.setAvailable(rs.getBoolean("is_available"));
        r.setDescription(rs.getString("description"));
        r.setRoomSize(rs.getString("room_size"));
        r.setNumBeds(rs.getInt("num_beds"));
        r.setMaxPersons(rs.getInt("max_persons"));
        r.setImageUrl(rs.getString("image_url"));
        return r;
    }
}