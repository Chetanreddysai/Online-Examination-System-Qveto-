package com.qveto.exam.model;

/**
 * Indicates how an examination session was completed.
 */
public enum SubmissionType {
    MANUAL,
    AUTO_TIMEOUT;

    public static SubmissionType fromString(String typeStr) {
        if (typeStr == null) return null;
        try {
            return SubmissionType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
