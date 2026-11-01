
package com.scc.dao;

import com.scc.model.Role;
import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    // ==========================================
    // REGISTER ACCOUNT
    // ==========================================

    // All registered accounts start with PENDING status.
    public boolean register(String username, String password, Role role) {

        String sql = """
            INSERT INTO users (username, password, roles, status)
            VALUES (?, ?, ?, 'PENDING')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.name());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // LOGIN
    // ==========================================

    // Only APPROVED accounts can log in.
    public Optional<User> login(String username, String password) {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE username = ?
            AND password = ?
            AND status = 'APPROVED'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return Optional.empty();
    }

    // ==========================================
    // CHECK USERNAME
    // ==========================================

    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // CREATE ACCOUNT BY ADMIN
    // ==========================================

    // Accounts created directly by Admin are APPROVED.
    public boolean createUser(
            String username,
            String password,
            Role role) {

        String sql = """
            INSERT INTO users (username, password, roles, status)
            VALUES (?, ?, ?, 'APPROVED')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.name());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Create account error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // VIEW ALL USERS
    // ==========================================

    public List<User> getAllUsers() {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            ORDER BY id
        """;

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return users;
    }

    // ==========================================
    // VIEW USERS BY ROLE
    // ==========================================

    public List<User> getUsersByRole(Role role) {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE roles = ?
            ORDER BY id
        """;

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.name());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return users;
    }

    // ==========================================
    // VIEW PENDING USERS
    // ==========================================

    public List<User> getPendingUsers() {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE status = 'PENDING'
            ORDER BY id
        """;

        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return users;
    }

    // ==========================================
    // APPROVE USER ACCOUNT
    // ==========================================

    public boolean approveUser(int userId) {

        String sql = """
            UPDATE users
            SET status = 'APPROVED'
            WHERE id = ?
            AND status = 'PENDING'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Approval error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // UPDATE USER ROLE AND STATUS
    // ==========================================

    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus) {

        try {
            Role.valueOf(newRole.trim().toUpperCase());

            String status = newStatus.trim().toUpperCase();

            if (!status.equals("APPROVED")
                    && !status.equals("PENDING")) {
                return false;
            }

            String sql = """
                UPDATE users
                SET roles = ?, status = ?
                WHERE id = ?
            """;

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, newRole.trim().toUpperCase());
                ps.setString(2, status);
                ps.setInt(3, userId);

                return ps.executeUpdate() == 1;
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role.");
            return false;

        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // DELETE USER ACCOUNT
    // ==========================================

    public boolean deleteUser(int userId) {

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // FIND USER BY ID
    // ==========================================

    public Optional<User> getUserById(int userId) {

        String sql = """
            SELECT id, username, password, roles, status
            FROM users
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return Optional.empty();
    }

    // ==========================================
    // MAP DATABASE RECORD TO USER OBJECT
    // ==========================================

    private User mapUser(ResultSet rs) throws SQLException {

        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                Role.valueOf(rs.getString("roles")),
                rs.getString("status")
        );
    }
}
