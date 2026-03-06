package com.oceanview.dao;

import com.oceanview.util.DBConnection;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void log(int userId, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, details, timestamp) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<JSONObject> getAllLogs() {
        List<JSONObject> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.username FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.timestamp DESC LIMIT 100";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("logId", rs.getInt("log_id"));
                obj.put("username", rs.getString("username") != null ? rs.getString("username") : "System");
                obj.put("action", rs.getString("action"));
                obj.put("details", rs.getString("details") != null ? rs.getString("details") : "");
                obj.put("timestamp", rs.getString("timestamp"));
                logs.add(obj);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return logs;
    }

    public boolean clearLogs() {
        String sql = "DELETE FROM audit_logs"; //f
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            // Log the clear action itself
            log(0, "CLEAR_AUDIT_LOGS", "All audit logs cleared by admin");
            return true;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}