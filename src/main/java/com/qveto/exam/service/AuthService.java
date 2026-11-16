package com.qveto.exam.service;

import com.qveto.exam.dao.UserDAO;
import com.qveto.exam.model.Role;
import com.qveto.exam.model.User;
import com.qveto.exam.util.DBConnection;
import com.qveto.exam.util.PasswordUtil;
import com.qveto.exam.util.SessionManager;
import com.qveto.exam.validation.InputValidator;
import com.qveto.exam.validation.ValidationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service orchestrating authentication, registration, and session initialization.
 * Integrates JDBC UserDAO with an in-memory synthetic fallback for seamless offline
 * demo capabilities when the MySQL server is not locally running.
 */
public class AuthService {

    private final UserDAO userDAO;

    // In-memory fallback accounts for offline demonstration
    private static final Map<String, User> mockUsers = new ConcurrentHashMap<>();
    private static final Map<String, String> mockPasswords = new ConcurrentHashMap<>();

    static {
        // Initialize default demo users matching seed.sql
        User admin = new User(1, "System Administrator", "admin@qveto.com", Role.ADMIN, "+91 98765 00001");
        mockUsers.put(admin.getEmail(), admin);
        mockPasswords.put(admin.getEmail(), "Admin@123");

        User student1 = new User(2, "Aarav Patel", "student1@qveto.com", Role.STUDENT, "+91 98765 00002");
        mockUsers.put(student1.getEmail(), student1);
        mockPasswords.put(student1.getEmail(), "Student@123");

        User student2 = new User(3, "Diya Sharma", "student2@qveto.com", Role.STUDENT, "+91 98765 00003");
        mockUsers.put(student2.getEmail(), student2);
        mockPasswords.put(student2.getEmail(), "Student@123");
    }

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param email user email
     * @param password raw password
     * @return User object on success, null on failure
     */
    public User login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();

        // 1. Try Live Database if reachable
        if (DBConnection.isDatabaseConnected()) {
            User dbUser = userDAO.authenticate(normalizedEmail, password);
            if (dbUser != null) {
                SessionManager.setCurrentUser(dbUser);
                return dbUser;
            }
        }

        // 2. Offline / Demo Fallback
        User mockUser = mockUsers.get(normalizedEmail);
        if (mockUser != null) {
            String expectedPass = mockPasswords.get(normalizedEmail);
            if (expectedPass != null && (expectedPass.equals(password) || PasswordUtil.verify(password, PasswordUtil.hash(expectedPass)))) {
                SessionManager.setCurrentUser(mockUser);
                return mockUser;
            }
        }

        return null;
    }

    /**
     * Registers a new student account.
     */
    public ValidationResult register(String fullName, String email, String password, String confirmPassword, String phone) {
        ValidationResult validation = InputValidator.validateRegistration(fullName, email, password, confirmPassword);
        if (!validation.isValid()) {
            return validation;
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Check uniqueness in live database or mock store
        if (DBConnection.isDatabaseConnected()) {
            if (userDAO.findByEmail(normalizedEmail) != null) {
                return ValidationResult.failure("An account with this email already exists.");
            }

            User newUser = new User(0, fullName.trim(), normalizedEmail, Role.STUDENT, phone != null ? phone.trim() : null);
            boolean created = userDAO.register(newUser, password);
            if (!created) {
                return ValidationResult.failure("Failed to create account due to database error.");
            }
            return ValidationResult.success();
        }

        // Offline Fallback Registration
        if (mockUsers.containsKey(normalizedEmail)) {
            return ValidationResult.failure("An account with this email already exists.");
        }

        int newId = mockUsers.size() + 10;
        User newUser = new User(newId, fullName.trim(), normalizedEmail, Role.STUDENT, phone != null ? phone.trim() : null);
        mockUsers.put(normalizedEmail, newUser);
        mockPasswords.put(normalizedEmail, password);

        return ValidationResult.success();
    }

    /**
     * Updates profile details (display name and phone) for the given user.
     */
    public ValidationResult updateProfile(int userId, String fullName, String phone) {
        if (fullName == null || fullName.trim().length() < 2) {
            return ValidationResult.failure("Full name must be at least 2 characters.");
        }

        User current = SessionManager.getCurrentUser();
        if (current != null && current.getUserId() == userId) {
            current.setFullName(fullName.trim());
            current.setPhone(phone != null ? phone.trim() : null);
        }

        if (DBConnection.isDatabaseConnected()) {
            boolean updated = userDAO.updateProfile(userId, fullName, phone);
            if (!updated) {
                return ValidationResult.failure("Database error while updating profile.");
            }
        }

        // Also update in-memory mock store if found
        for (User u : mockUsers.values()) {
            if (u.getUserId() == userId) {
                u.setFullName(fullName.trim());
                u.setPhone(phone != null ? phone.trim() : null);
                break;
            }
        }

        return ValidationResult.success();
    }

    /**
     * Changes user's password after verifying existing password and confirmation.
     */
    public ValidationResult updatePassword(int userId, String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            return ValidationResult.failure("Current password is required.");
        }
        if (newPassword == null || !InputValidator.isValidPassword(newPassword)) {
            return ValidationResult.failure("New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ValidationResult.failure("New password and confirmation do not match.");
        }

        User current = SessionManager.getCurrentUser();
        String userEmail = current != null ? current.getEmail() : null;

        // Verify old password
        if (DBConnection.isDatabaseConnected() && userEmail != null) {
            User verified = userDAO.authenticate(userEmail, oldPassword);
            if (verified == null) {
                return ValidationResult.failure("Current password is incorrect.");
            }
            boolean changed = userDAO.updatePassword(userId, newPassword);
            if (!changed) {
                return ValidationResult.failure("Database error while updating password.");
            }
            if (current != null) {
                current.setPasswordHash(PasswordUtil.hash(newPassword));
            }
            return ValidationResult.success();
        }

        // Offline Fallback Verification
        if (userEmail != null) {
            String stored = mockPasswords.get(userEmail);
            if (stored == null || !stored.equals(oldPassword)) {
                return ValidationResult.failure("Current password is incorrect.");
            }
            mockPasswords.put(userEmail, newPassword);
            if (current != null) {
                current.setPasswordHash(PasswordUtil.hash(newPassword));
            }
            return ValidationResult.success();
        }

        return ValidationResult.failure("Active session not found.");
    }

    public void logout() {
        SessionManager.logout();
    }
}
