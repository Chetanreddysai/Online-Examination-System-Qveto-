package com.qveto.exam.model;

/**
 * Lifecycle states of an exam attempt.
 *
 * State Machine Transitions:
 *  - IN_PROGRESS -> SUBMITTED (via manual confirmed submission)
 *  - IN_PROGRESS -> TIMED_OUT (via countdown timer expiration)
 *  - IN_PROGRESS -> ABANDONED (via window closure / unexpected interruption)
 *  - Terminal states (SUBMITTED, TIMED_OUT, ABANDONED) cannot transition to any other state.
 */
public enum AttemptStatus {
    IN_PROGRESS("In Progress"),
    SUBMITTED("Submitted"),
    TIMED_OUT("Timed Out"),
    ABANDONED("Abandoned");

    private final String displayName;

    AttemptStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this status represents a terminal (immutable) state.
     */
    public boolean isTerminal() {
        return this != IN_PROGRESS;
    }

    /**
     * Validates whether a state transition from this state to the next state is permitted.
     */
    public boolean canTransitionTo(AttemptStatus next) {
        if (next == null) return false;
        if (this == next) return true;
        if (this.isTerminal()) {
            return false; // Terminal states cannot transition to anything
        }
        // From IN_PROGRESS, valid terminal states are SUBMITTED, TIMED_OUT, ABANDONED
        return next == SUBMITTED || next == TIMED_OUT || next == ABANDONED;
    }

    public static AttemptStatus fromString(String statusStr) {
        if (statusStr == null) return IN_PROGRESS;
        try {
            return AttemptStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return IN_PROGRESS;
        }
    }
}
