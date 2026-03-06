package com.oceanview.controller;

import com.oceanview.service.AuthService;
import com.oceanview.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {

    private AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/login") && exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleLogin(exchange);
        } else if (path.endsWith("/logout") && exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleLogout(exchange);
        } else {
            sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JSONObject json = JsonUtil.parseBody(body);

        String username = json.optString("username", "");
        String password = json.optString("password", "");

        String token = authService.login(username, password);
        if (token != null) {
            JSONObject data = new JSONObject();
            data.put("token", token);
            sendResponse(exchange, 200, JsonUtil.successResponse("Login successful", data));
        } else {
            sendResponse(exchange, 401, JsonUtil.errorResponse("Invalid username or password"));
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        sendResponse(exchange, 200, JsonUtil.successResponse("Logged out successfully"));
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
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