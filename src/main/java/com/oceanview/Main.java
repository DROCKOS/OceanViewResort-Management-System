package com.oceanview;

import com.oceanview.controller.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Auth
        server.createContext("/api/auth/login",   new AuthHandler());
        server.createContext("/api/auth/logout",  new AuthHandler());

        // Profile & Admin verify
        server.createContext("/api/profile",      new ProfileHandler());
        server.createContext("/api/auth/verify-admin", new ProfileHandler());

        // Reservations
        server.createContext("/api/reservations", new ReservationHandler());

        // Rooms
        server.createContext("/api/rooms",        new RoomHandler());

        // Bills
        server.createContext("/api/bills",        new BillHandler());

        // Audit
        server.createContext("/api/audit",        new AuditHandler());

        // Static files (web folder)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("✅ Ocean View Resort server started on http://localhost:8080");
        System.out.println("👤 Admin login  → username: admin  | password: Admin@1234");
        System.out.println("👤 Staff login  → username: staff  | password: Staff@1234");
    }
}