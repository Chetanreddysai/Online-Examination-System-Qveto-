package com.qveto.exam.model;

/**
 * Lifecycle states of an exam attempt.
 */
public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    TIMED_OUT,
    ABANDONED;

    public static AttemptStatus fromString(String statusStr) {
        if (statusStr == null) return IN_PROGRESS;
        try {
            return AttemptStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return IN_PROGRESS;
        }
    }
}
