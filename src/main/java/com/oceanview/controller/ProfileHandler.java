package com.oceanview.controller;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ProfileHandler implements HttpHandler {

    private UserDAO userDAO = new UserDAO();
    private AuthService authService = new AuthService();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

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
            if (path.equals("/api/profile") && method.equalsIgnoreCase("GET")) {
                handleGetProfile(exchange, user);
            } else if (path.equals("/api/profile/username") && method.equalsIgnoreCase("PUT")) {
                handleUpdateUsername(exchange, user);
            } else if (path.equals("/api/profile/password") && method.equalsIgnoreCase("PUT")) {
                handleUpdatePassword(exchange, user);
            } else if (path.equals("/api/auth/verify-admin") && method.equalsIgnoreCase("POST")) {
                handleVerifyAdmin(exchange);
            } else {
                sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Server error"));
        }
    }

    private void handleGetProfile(HttpExchange exchange, User user) throws IOException {
        JSONObject data = new JSONObject();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("data", data);
        sendResponse(exchange, 200, resp.toString());
    }

    private void handleUpdateUsername(HttpExchange exchange, User user) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String newUsername = json.optString("username", "").trim();
        String currentPassword = json.optString("currentPassword", "");

        if (newUsername.isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("Username cannot be empty"));
            return;
        }
        if (newUsername.length() < 3) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("Username must be at least 3 characters"));
            return;
        }

        // Verify current password before changing
        if (!userDAO.verifyPassword(user.getId(), currentPassword)) {
            sendResponse(exchange, 401, JsonUtil.errorResponse("Current password is incorrect"));
            return;
        }

        boolean success = userDAO.updateUsername(user.getId(), newUsername);
        if (success) {
            auditLogDAO.log(user.getId(), "UPDATE_USERNAME",
                    "Username changed from " + user.getUsername() + " to " + newUsername);
            sendResponse(exchange, 200, JsonUtil.successResponse("Username updated successfully"));
        } else {
            sendResponse(exchange, 400, JsonUtil.errorResponse("Username already taken"));
        }
    }

    private void handleUpdatePassword(HttpExchange exchange, User user) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String currentPassword = json.optString("currentPassword", "");
        String newPassword = json.optString("newPassword", "");
        String confirmPassword = json.optString("confirmPassword", "");

        if (newPassword.isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("New password cannot be empty"));
            return;
        }
        if (newPassword.length() < 6) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("Password must be at least 6 characters"));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("Passwords do not match"));
            return;
        }
        if (!userDAO.verifyPassword(user.getId(), currentPassword)) {
            sendResponse(exchange, 401, JsonUtil.errorResponse("Current password is incorrect"));
            return;
        }

        boolean success = userDAO.updatePassword(user.getId(), newPassword);
        if (success) {
            auditLogDAO.log(user.getId(), "UPDATE_PASSWORD",
                    user.getUsername() + " changed their password");
            sendResponse(exchange, 200, JsonUtil.successResponse("Password updated successfully"));
        } else {
            sendResponse(exchange, 500, JsonUtil.errorResponse("Failed to update password"));
        }
    }

    private void handleVerifyAdmin(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        String username = json.optString("username", "");
        String password = json.optString("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            sendResponse(exchange, 400, JsonUtil.errorResponse("Username and password required"));
            return;
        }

        boolean valid = userDAO.verifyAdminCredentials(username, password);
        if (valid) {
            sendResponse(exchange, 200, JsonUtil.successResponse("Admin verified"));
        } else {
            sendResponse(exchange, 401, JsonUtil.errorResponse("Invalid admin credentials"));
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}