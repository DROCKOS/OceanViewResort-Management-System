package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;
import com.oceanview.util.PasswordUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private static final Map<String, Integer> sessions = new ConcurrentHashMap<>();
    private UserDAO userDAO = new UserDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    public String login(String username, String password) {
        if (username == null || password == null) return null;
        User user = userDAO.getUserByUsername(username.trim());
        if (user == null) return null;
        String hashed = PasswordUtil.hash(password);
        if (!hashed.equals(user.getPasswordHash())) return null;
        // Generate token
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        auditLogDAO.log(user.getId(), "LOGIN", user.getUsername() + " logged in");
        return token;
    }
    public void logout(String token) {
        if (token != null && sessions.containsKey(token)) {
            int userId = sessions.get(token);
            User user = userDAO.getUserById(userId);
            if (user != null) {
                auditLogDAO.log(userId, "LOGOUT", user.getUsername() + " logged out");
            }
            sessions.remove(token);
        }
    }
    public boolean isValidSession(String token) {
        return token != null && sessions.containsKey(token);
    }
    public User getUserByToken(String token) {
        if (token == null) return null;
        Integer userId = sessions.get(token);
        if (userId == null) return null;
        return userDAO.getUserById(userId);
    }
    public String getUserRole(String token) {
        User user = getUserByToken(token);
        if (user == null) return null;
        return user.getRole();
    }
}