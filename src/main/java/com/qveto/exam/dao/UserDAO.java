package com.qveto.exam.dao;

import com.qveto.exam.model.Role;
import com.qveto.exam.model.User;
import com.qveto.exam.util.DBConnection;
import com.qveto.exam.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for user accounts (Admin and Student).
 * All queries utilize PreparedStatements to guarantee protection against SQL injection.
 */
public class UserDAO {

    /**
     * Authenticates a user by email and plain-text password using SHA-256 verification.
     */
    public User authenticate(String email, String plainPassword) {
        String sql = "SELECT user_id, full_name, email, password_hash, role, phone, created_at " +
                     "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verify(plainPassword, storedHash)) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Authentication database error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a user by their email address.
     */
    public User findByEmail(String email) {
        String sql = "SELECT user_id, full_name, email, password_hash, role, phone, created_at " +
                     "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] findByEmail error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registers a new user with a securely hashed password.
     */
    public boolean register(User user, String plainPassword) {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, phone) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFullName().trim());
            stmt.setString(2, user.getEmail().trim().toLowerCase());
            stmt.setString(3, PasswordUtil.hash(plainPassword));
            stmt.setString(4, user.getRole() != null ? user.getRole().name() : Role.STUDENT.name());
            stmt.setString(5, user.getPhone());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] register error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates user's password.
     */
    public boolean updatePassword(int userId, String newPlainPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtil.hash(newPlainPassword));
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Fetches all registered students.
     */
    public List<User> getAllStudents() {
        List<User> students = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, password_hash, role, phone, created_at " +
                     "FROM users WHERE role = 'STUDENT' ORDER BY full_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllStudents error: " + e.getMessage());
        }
        return students;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setPhone(rs.getString("phone"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
