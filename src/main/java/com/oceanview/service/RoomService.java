package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Room;
import com.oceanview.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class RoomService {

    private RoomDAO roomDAO = new RoomDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    public List<Room> getAllRooms() {
        return roomDAO.getAllRooms();
    }

    public List<Room> getAvailableRooms(String checkIn, String checkOut) {
        return roomDAO.getAvailableRooms(checkIn, checkOut);
    }

    public Room getRoomById(int roomId) {
        return roomDAO.getRoomById(roomId);
    }

    public String addRoom(String roomNumber, String roomType, double pricePerNight,
                          String description, String roomSize, int numBeds,
                          int maxPersons, String imageUrl, int userId) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) return "Room number is required";
        if (roomType == null || roomType.trim().isEmpty()) return "Room type is required";
        if (pricePerNight <= 0) return "Price must be greater than zero";

        Room room = new Room();
        room.setRoomNumber(roomNumber.trim());
        room.setRoomType(roomType.trim());
        room.setPricePerNight(pricePerNight);
        room.setDescription(description);
        room.setRoomSize(roomSize);
        room.setNumBeds(numBeds);
        room.setMaxPersons(maxPersons);
        room.setImageUrl(imageUrl);
        room.setAvailable(true);

        boolean success = roomDAO.addRoom(room);
        if (success) {
            auditLogDAO.log(userId, "ADD_ROOM", "Added room " + roomNumber + " (" + roomType + ")");
            return "SUCCESS";
        }
        return "Failed to add room";
    }

    public String updateRoom(int roomId, String roomType, double pricePerNight,
                             String description, boolean isAvailable, String roomSize,
                             int numBeds, int maxPersons, String imageUrl, int userId) {
        Room room = roomDAO.getRoomById(roomId);
        if (room == null) return "Room not found";

        room.setRoomType(roomType);
        room.setPricePerNight(pricePerNight);
        room.setDescription(description);
        room.setAvailable(isAvailable);
        room.setRoomSize(roomSize);
        room.setNumBeds(numBeds);
        room.setMaxPersons(maxPersons);
        room.setImageUrl(imageUrl);

        boolean success = roomDAO.updateRoom(room);
        if (success) {
            auditLogDAO.log(userId, "UPDATE_ROOM", "Updated room " + room.getRoomNumber());
            return "SUCCESS";
        }
        return "Failed to update room";
    }

    public String deleteRoom(int roomId, int userId) {
        Room room = roomDAO.getRoomById(roomId);
        if (room == null) return "Room not found";

        // Block deletion if this room has any reservations (active or historical)
        int reservationCount = getReservationCount(roomId);
        if (reservationCount < 0) {
            // -1 means the check itself failed — block to be safe
            return "Unable to verify room reservations. Please try again.";
        }
        if (reservationCount > 0) {
            return "Cannot delete Room " + room.getRoomNumber() +
                    " — it has " + reservationCount + " reservation(s) linked to it. " +
                    "Please cancel or remove those reservations first.";
        }

        boolean success = roomDAO.deleteRoom(roomId);
        if (success) {
            auditLogDAO.log(userId, "DELETE_ROOM", "Deleted room " + room.getRoomNumber());
            return "SUCCESS";
        }
        return "Failed to delete room";
    }

    private int getReservationCount(int roomId) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // signals failure
        }
        return 0;
    }
}