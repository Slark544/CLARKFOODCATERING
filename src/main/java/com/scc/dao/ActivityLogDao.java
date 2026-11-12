package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Automatically create activity_logs table.
    public void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT NOT NULL
                        DEFAULT (datetime('now', 'localtime'))
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_activity_logs_user
                    ON activity_logs(user_id)
                    """);

        } catch (SQLException e) {
            System.out.println(
                    "Activity log table error: " + e.getMessage()
            );
        }
    }

    // Record an activity.
    public boolean logAction(
            int userId,
            String actionType,
            String description
    ) {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Logging error: " + e.getMessage()
            );
            return false;
        }
    }

    // Get all system logs.
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT *
                FROM activity_logs
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapLog(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving logs: " + e.getMessage()
            );
        }

        return logs;
    }

    // Get logs belonging to a specific user.
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT *
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, log_id DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    logs.add(mapLog(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error filtering logs: " + e.getMessage()
            );
        }

        return logs;
    }

    // Clear historical logs.
    public boolean clearLogs() {

        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Error clearing logs: " + e.getMessage()
            );
            return false;
        }
    }

    // Convert database record into ActivityLog.
    private ActivityLog mapLog(ResultSet rs) throws SQLException {

        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}