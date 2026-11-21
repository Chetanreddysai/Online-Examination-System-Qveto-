package com.qveto.exam.model;

/**
 * Indicates how an examination session was completed.
 */
public enum SubmissionType {
    MANUAL("Manual Submission"),
    AUTO_TIMEOUT("Auto Submission (Timer Expiry)");

    private final String displayName;

    SubmissionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isManual() {
        return this == MANUAL;
    }

    public boolean isAutoTimeout() {
        return this == AUTO_TIMEOUT;
    }

    public static SubmissionType fromString(String typeStr) {
        if (typeStr == null) return null;
        try {
            return SubmissionType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
