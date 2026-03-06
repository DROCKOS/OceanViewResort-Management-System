package com.oceanview.controller;

import com.oceanview.model.Bill;
import com.oceanview.service.AuthService;
import com.oceanview.service.BillService;
import com.oceanview.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BillHandler implements HttpHandler {

    private BillService billService = new BillService();
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
            if (path.equals("/api/bills") && method.equalsIgnoreCase("GET")) {
                handleGetAll(exchange);
            } else if (path.matches("/api/bills/\\d+") && method.equalsIgnoreCase("GET")) {
                handleGetByReservation(exchange, extractId(path));
            } else if (path.matches("/api/bills/\\d+/generate") && method.equalsIgnoreCase("POST")) {
                handleGenerate(exchange, extractId(path), userId);
            } else if (path.matches("/api/bills/\\d+/pay") && method.equalsIgnoreCase("PUT")) {
                handlePay(exchange, extractId(path), userId);
            } else {
                sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Server error"));
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Bill> bills = billService.getAllBills();
        org.json.JSONArray arr = new org.json.JSONArray();
        for (Bill b : bills) arr.put(billToJson(b));
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", arr);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleGetByReservation(HttpExchange exchange, int reservationId) throws IOException {
        Bill bill = billService.getBillByReservationId(reservationId);
        if (bill == null) {
            sendResponse(exchange, 404, JsonUtil.errorResponse("No bill found"));
            return;
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", billToJson(bill));
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleGenerate(HttpExchange exchange, int reservationId, int userId) throws IOException {
        String result = billService.generateBill(reservationId, userId);
        if (result.startsWith("SUCCESS:")) {
            int billId = Integer.parseInt(result.split(":")[1]);
            Bill bill = billService.getBillByReservationId(reservationId);
            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("message", "Bill generated successfully");
            resp.put("data", billToJson(bill));
            sendResponse(exchange, 201, resp.toString());
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private void handlePay(HttpExchange exchange, int billId, int userId) throws IOException {
        String result = billService.markAsPaid(billId, userId);
        if (result.equals("SUCCESS")) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Bill marked as paid"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse(result));
        }
    }

    private JSONObject billToJson(Bill b) {
        JSONObject json = new JSONObject();
        json.put("billId", b.getBillId());
        json.put("reservationId", b.getReservationId());
        json.put("guestName", b.getGuestName() != null ? b.getGuestName() : "");
        json.put("roomNumber", b.getRoomNumber() != null ? b.getRoomNumber() : "");
        json.put("roomType", b.getRoomType() != null ? b.getRoomType() : "");
        json.put("pricePerNight", b.getPricePerNight());
        json.put("checkInDate", b.getCheckInDate() != null ? b.getCheckInDate() : "");
        json.put("checkOutDate", b.getCheckOutDate() != null ? b.getCheckOutDate() : "");
        json.put("numberOfNights", b.getNumberOfNights());
        json.put("totalAmount", b.getTotalAmount());
        json.put("isPaid", b.isPaid());
        json.put("generatedDate", b.getGeneratedDate() != null ? b.getGeneratedDate() : "");
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}