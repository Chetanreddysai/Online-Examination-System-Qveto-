package com.qveto.exam.util;

import com.qveto.exam.model.Role;
import com.qveto.exam.model.User;

/**
 * Thread-safe global session manager holding the active logged-in user state.
 */
public final class SessionManager {

    private static volatile User currentUser = null;

    private SessionManager() {}

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public static synchronized boolean isStudent() {
        return currentUser != null && currentUser.getRole() == Role.STUDENT;
    }

    public static synchronized void logout() {
        currentUser = null;
    }
}
