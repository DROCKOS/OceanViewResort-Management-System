package com.oceanview.controller;

import com.oceanview.model.Reservation;
import com.oceanview.service.AuthService;
import com.oceanview.service.ReservationService;
import com.oceanview.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReservationHandler implements HttpHandler {

    private ReservationService reservationService = new ReservationService();
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
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/reservations") && method.equalsIgnoreCase("GET")) {
                handleGetAll(exchange);
            } else if (path.equals("/api/reservations") && method.equalsIgnoreCase("POST")) {
                handleCreate(exchange, userId);
            } else if (path.equals("/api/reservations/calendar") && method.equalsIgnoreCase("GET")) {
                handleGetCalendar(exchange);
            } else if (path.matches("/api/reservations/\\d+") && method.equalsIgnoreCase("GET")) {
                handleGetOne(exchange, extractId(path));
            } else if (path.matches("/api/reservations/\\d+") && method.equalsIgnoreCase("PUT")) {
                handleUpdate(exchange, extractId(path), userId);
            } else if (path.matches("/api/reservations/\\d+/cancel") && method.equalsIgnoreCase("PUT")) {
                handleCancel(exchange, extractId(path), userId);
            } else if (path.matches("/api/reservations/\\d+/delete") && method.equalsIgnoreCase("DELETE")) {
                handleDelete(exchange, extractId(path), userId);
            } else {
                sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Server error"));
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Reservation> list = reservationService.getAllReservations();
        JSONArray arr = new JSONArray();
        for (Reservation r : list) arr.put(reservationToJson(r));
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", arr);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleGetCalendar(HttpExchange exchange) throws IOException {
        List<Reservation> list = reservationService.getConfirmedReservations();
        JSONArray arr = new JSONArray();
        for (Reservation r : list) {
            JSONObject obj = new JSONObject();
            obj.put("reservationId", r.getReservationId());
            obj.put("guestName", r.getGuestName());
            obj.put("roomNumber", r.getRoomNumber());
            obj.put("roomType", r.getRoomType());
            obj.put("checkInDate", r.getCheckInDate());
            obj.put("checkOutDate", r.getCheckOutDate());
            arr.put(obj);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", arr);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleGetOne(HttpExchange exchange, int id) throws IOException {
        Reservation r = reservationService.getReservation(id);
        if (r == null) {
            sendResponse(exchange, 404, JsonUtil.errorResponse("Reservation not found"));
            return;
        }
        sendResponse(exchange, 200, JsonUtil.successResponse("OK", reservationToJson(r)));
    }

    private void handleCreate(HttpExchange exchange, int userId) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String result = reservationService.createReservation(
                json.optString("guestName"),
                json.optString("address"),
                json.optString("contactNumber"),
                json.optString("email"),
                json.optInt("roomId"),
                json.optString("checkInDate"),
                json.optString("checkOutDate"),
                userId
        );
        if (result.startsWith("SUCCESS:")) {
            int newId = Integer.parseInt(result.split(":")[1]);
            JSONObject data = new JSONObject();
            data.put("reservationId", newId);
            sendResponse(exchange, 201, JsonUtil.successResponse("Reservation created", data));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private void handleUpdate(HttpExchange exchange, int id, int userId) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String result = reservationService.updateReservation(
                id,
                json.optInt("roomId"),
                json.optString("checkInDate"),
                json.optString("checkOutDate"),
                userId
        );
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Reservation updated"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private void handleCancel(HttpExchange exchange, int id, int userId) throws IOException {
        String result = reservationService.cancelReservation(id, userId);
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Reservation cancelled"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private void handleDelete(HttpExchange exchange, int id, int userId) throws IOException {
        String result = reservationService.deleteReservation(id, userId);
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Reservation deleted"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private JSONObject reservationToJson(Reservation r) {
        JSONObject json = new JSONObject();
        json.put("reservationId", r.getReservationId());
        json.put("guestId", r.getGuestId());
        json.put("guestName", r.getGuestName());
        json.put("roomId", r.getRoomId());
        json.put("roomNumber", r.getRoomNumber());
        json.put("roomType", r.getRoomType());
        json.put("pricePerNight", r.getPricePerNight());
        json.put("checkInDate", r.getCheckInDate());
        json.put("checkOutDate", r.getCheckOutDate());
        json.put("status", r.getStatus());
        json.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt() : "");
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}