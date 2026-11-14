package com.qveto.exam.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the outcome of an input validation check.
 */
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public static ValidationResult success() {
        return new ValidationResult();
    }

    public static ValidationResult failure(String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.addError(errorMessage);
        return result;
    }

    public void addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            errors.add(error.trim());
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public String getFirstError() {
        return errors.isEmpty() ? "" : errors.get(0);
    }

    public String getJoinedErrors() {
        return String.join("\n• ", errors);
    }
}
