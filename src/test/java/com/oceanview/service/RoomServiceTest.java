package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock private RoomDAO roomDAO;
    @Mock private AuditLogDAO auditLogDAO;

    @InjectMocks
    private RoomService roomService;

    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        sampleRoom = new Room(1, "101", "Deluxe", 5000.0, true,
                "Sea view room", "35 m²", 1, 2, "");
    }

    // ── addRoom ───────────────────────────────────────────────────────────
    @Test
    void testAddRoomSuccess() {
        when(roomDAO.addRoom(any(Room.class))).thenReturn(true);
        String result = roomService.addRoom("101", "Deluxe", 5000.0,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testAddRoomFailsWithZeroPrice() {
        String result = roomService.addRoom("101", "Deluxe", 0,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertNotEquals("SUCCESS", result);
        assertTrue(result.toLowerCase().contains("price"));
    }

    @Test
    void testAddRoomFailsWithNegativePrice() {
        String result = roomService.addRoom("101", "Deluxe", -100,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertNotEquals("SUCCESS", result);
    }

    @Test
    void testAddRoomFailsWithEmptyRoomNumber() {
        String result = roomService.addRoom("", "Deluxe", 5000.0,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertNotEquals("SUCCESS", result);
    }

    @Test
    void testAddRoomFailsWithNullRoomNumber() {
        String result = roomService.addRoom(null, "Deluxe", 5000.0,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertNotEquals("SUCCESS", result);
    }

    @Test
    void testAddRoomFailsWithEmptyRoomType() {
        String result = roomService.addRoom("102", "", 5000.0,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertNotEquals("SUCCESS", result);
    }

    @Test
    void testAddRoomFailsWithNullRoomType() {
        String result = roomService.addRoom("103", null, 5000.0,
                "Sea view", "35 m²", 1, 2, "", 1);
        assertNotEquals("SUCCESS", result);
    }

    // ── updateRoom ────────────────────────────────────────────────────────
    @Test
    void testUpdateRoomSuccess() {
        when(roomDAO.getRoomById(1)).thenReturn(sampleRoom);
        when(roomDAO.updateRoom(any(Room.class))).thenReturn(true);
        String result = roomService.updateRoom(1, "Suite", 8000.0,
                "Updated desc", true, "40 m²", 2, 3, "", 1);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testUpdateRoomNotFound() {
        when(roomDAO.getRoomById(99)).thenReturn(null);
        String result = roomService.updateRoom(99, "Suite", 8000.0,
                "desc", true, "40 m²", 2, 3, "", 1);
        assertEquals("Room not found", result);
    }

    @Test
    void testUpdateRoomSetUnavailable() {
        when(roomDAO.getRoomById(1)).thenReturn(sampleRoom);
        when(roomDAO.updateRoom(any(Room.class))).thenReturn(true);
        String result = roomService.updateRoom(1, "Deluxe", 5000.0,
                "Sea view", false, "35 m²", 1, 2, "", 1);
        assertEquals("SUCCESS", result);
    }

    // ── getRoomById ───────────────────────────────────────────────────────
    @Test
    void testGetRoomByIdReturnsRoom() {
        when(roomDAO.getRoomById(1)).thenReturn(sampleRoom);
        Room result = roomService.getRoomById(1);
        assertNotNull(result);
        assertEquals("101", result.getRoomNumber());
    }

    @Test
    void testGetRoomByIdNotFound() {
        when(roomDAO.getRoomById(99)).thenReturn(null);
        assertNull(roomService.getRoomById(99));
    }

    // ── getAllRooms ───────────────────────────────────────────────────────
    @Test
    void testGetAllRoomsReturnsList() {
        when(roomDAO.getAllRooms()).thenReturn(Arrays.asList(sampleRoom));
        List<Room> rooms = roomService.getAllRooms();
        assertEquals(1, rooms.size());
    }

    @Test
    void testGetAllRoomsEmpty() {
        when(roomDAO.getAllRooms()).thenReturn(List.of());
        List<Room> rooms = roomService.getAllRooms();
        assertTrue(rooms.isEmpty());
    }

    // ── deleteRoom ────────────────────────────────────────────────────────
    @Test
    void testDeleteRoomNotFound() {
        when(roomDAO.getRoomById(99)).thenReturn(null);
        String result = roomService.deleteRoom(99, 1);
        assertEquals("Room not found", result);
    }

    @Test
    void testDeleteRoomSuccess() {
        // Room with no reservations can be deleted
        Room roomNoReservations = new Room(5, "505", "Standard", 3000.0,
                true, "Basic room", "25 m²", 1, 2, "");
        when(roomDAO.getRoomById(5)).thenReturn(roomNoReservations);
        when(roomDAO.deleteRoom(5)).thenReturn(true);
        String result = roomService.deleteRoom(5, 1);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testRoomPriceIsStoredCorrectly() {
        when(roomDAO.getRoomById(1)).thenReturn(sampleRoom);
        Room result = roomService.getRoomById(1);
        assertEquals(5000.0, result.getPricePerNight());
    }
}
