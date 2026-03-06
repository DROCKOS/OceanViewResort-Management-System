package com.oceanview.controller;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AuditHandler implements HttpHandler {

    private AuditLogDAO auditLogDAO = new AuditLogDAO();
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

        User user = authService.getUserByToken(token);
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.equals("/api/audit") && method.equalsIgnoreCase("GET")) {
                handleGetAll(exchange);
            } else if (path.equals("/api/audit/clear") && method.equalsIgnoreCase("DELETE")) {
                handleClear(exchange, user);
            } else {
                sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Server error"));
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<JSONObject> logs = auditLogDAO.getAllLogs();
        JSONArray arr = new JSONArray(logs);
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", arr);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleClear(HttpExchange exchange, User user) throws IOException {
        // Only ADMIN can clear logs
        if (!user.getRole().equals("ADMIN")) {
            sendResponse(exchange, 403, JsonUtil.errorResponse("Admin access required"));
            return;
        }
        boolean success = auditLogDAO.clearLogs();
        if (success) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Audit logs cleared"));
        } else {
            sendResponse(exchange, 500, JsonUtil.errorResponse("Failed to clear logs"));
        }
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}