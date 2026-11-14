package com.qveto.exam.model;

/**
 * Enumeration representing user roles in the Online Examination System.
 */
public enum Role {
    ADMIN,
    STUDENT;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return STUDENT;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT;
        }
    }
}
