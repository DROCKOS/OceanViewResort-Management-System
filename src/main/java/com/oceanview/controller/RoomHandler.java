package com.oceanview.controller;

import com.oceanview.model.Room;
import com.oceanview.service.AuthService;
import com.oceanview.service.RoomService;
import com.oceanview.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RoomHandler implements HttpHandler {

    private RoomService roomService = new RoomService();
    private AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String token = getToken(exchange);
        if (!authService.isValidSession(token)) {
            sendResponse(exchange, 401, JsonUtil.errorResponse("Unauthorized"));
            return;
        }

        int userId = authService.getUserByToken(token).getId();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/rooms") && method.equalsIgnoreCase("GET")) {
                handleGetAll(exchange);
            } else if (path.equals("/api/rooms/available") && method.equalsIgnoreCase("GET")) {
                handleGetAvailable(exchange, query);
            } else if (path.equals("/api/rooms") && method.equalsIgnoreCase("POST")) {
                handleAdd(exchange, userId);
            } else if (path.matches("/api/rooms/\\d+") && method.equalsIgnoreCase("PUT")) {
                int roomId = extractId(path);
                handleUpdate(exchange, roomId, userId);
            } else if (path.matches("/api/rooms/\\d+") && method.equalsIgnoreCase("DELETE")) {
                int roomId = extractId(path);
                handleDelete(exchange, roomId, userId, token);
            } else {
                sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Server error"));
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Room> rooms = roomService.getAllRooms();
        org.json.JSONArray arr = new org.json.JSONArray();
        for (Room r : rooms) arr.put(roomToJson(r));
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", arr);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleGetAvailable(HttpExchange exchange, String query) throws IOException {
        String checkIn = null, checkOut = null;
        if (query != null) {
            for (String p : query.split("&")) {
                if (p.startsWith("checkin="))  checkIn  = p.substring(8);
                if (p.startsWith("checkout=")) checkOut = p.substring(9);
            }
        }
        if (checkIn == null || checkOut == null) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("checkin and checkout are required"));
            return;
        }
        List<Room> rooms = roomService.getAvailableRooms(checkIn, checkOut);
        org.json.JSONArray arr = new org.json.JSONArray();
        for (Room r : rooms) arr.put(roomToJson(r));
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", arr);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleAdd(HttpExchange exchange, int userId) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String result = roomService.addRoom(
                json.optString("roomNumber"),
                json.optString("roomType"),
                json.optDouble("pricePerNight", 0),
                json.optString("description"),
                json.optString("roomSize"),
                json.optInt("numBeds", 1),
                json.optInt("maxPersons", 2),
                json.optString("imageUrl"),
                userId
        );
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 201, JsonUtil.successResponse("Room added successfully"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private void handleUpdate(HttpExchange exchange, int roomId, int userId) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String result = roomService.updateRoom(
                roomId,
                json.optString("roomType"),
                json.optDouble("pricePerNight", 0),
                json.optString("description"),
                json.optBoolean("isAvailable", true),
                json.optString("roomSize"),
                json.optInt("numBeds", 1),
                json.optInt("maxPersons", 2),
                json.optString("imageUrl"),
                userId
        );
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Room updated successfully"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private void handleDelete(HttpExchange exchange, int roomId, int userId, String token) throws IOException {
        // Only ADMIN can delete rooms
        String role = authService.getUserRole(token);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            sendResponse(exchange, 403, JsonUtil.errorResponse(
                    "Access denied. Only administrators can delete rooms."));
            return;
        }

        String result = roomService.deleteRoom(roomId, userId);
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Room deleted successfully"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private JSONObject roomToJson(Room r) {
        JSONObject json = new JSONObject();
        json.put("roomId",       r.getRoomId());
        json.put("roomNumber",   r.getRoomNumber());
        json.put("roomType",     r.getRoomType());
        json.put("pricePerNight",r.getPricePerNight());
        json.put("isAvailable",  r.isAvailable());
        json.put("description",  r.getDescription()  != null ? r.getDescription()  : "");
        json.put("roomSize",     r.getRoomSize()      != null ? r.getRoomSize()      : "");
        json.put("numBeds",      r.getNumBeds());
        json.put("maxPersons",   r.getMaxPersons());
        json.put("imageUrl",     r.getImageUrl()      != null ? r.getImageUrl()      : "");
        return json;
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        for (String part : parts) {
            try { return Integer.parseInt(part); } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}