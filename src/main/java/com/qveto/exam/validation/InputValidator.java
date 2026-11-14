package com.qveto.exam.validation;

import java.util.regex.Pattern;

/**
 * Validates user inputs across authentication, exam management, and question authoring.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private InputValidator() {}

    /**
     * Validates an email address.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates password strength (minimum 6 characters).
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates student registration input fields.
     */
    public static ValidationResult validateRegistration(String fullName, String email, String password, String confirmPassword) {
        ValidationResult result = new ValidationResult();

        if (fullName == null || fullName.trim().isEmpty()) {
            result.addError("Full name is required.");
        } else if (fullName.trim().length() < 2) {
            result.addError("Full name must be at least 2 characters.");
        }

        if (email == null || email.trim().isEmpty()) {
            result.addError("Email address is required.");
        } else if (!isValidEmail(email)) {
            result.addError("Please enter a valid email address (e.g., student@qveto.com).");
        }

        if (password == null || password.isEmpty()) {
            result.addError("Password is required.");
        } else if (!isValidPassword(password)) {
            result.addError("Password must be at least 6 characters long.");
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
            result.addError("Passwords do not match.");
        }

        return result;
    }

    /**
     * Validates login inputs.
     */
    public static ValidationResult validateLogin(String email, String password) {
        ValidationResult result = new ValidationResult();
        if (email == null || email.trim().isEmpty()) {
            result.addError("Please enter your email.");
        } else if (!isValidEmail(email)) {
            result.addError("Please enter a valid email address.");
        }

        if (password == null || password.isEmpty()) {
            result.addError("Please enter your password.");
        }
        return result;
    }

    /**
     * Validates question bank authoring inputs.
     */
    public static ValidationResult validateQuestion(String subject, String text, String optA, String optB, String optC, String optD, String correctOpt) {
        ValidationResult result = new ValidationResult();

        if (subject == null || subject.trim().isEmpty()) {
            result.addError("Subject is required.");
        }
        if (text == null || text.trim().isEmpty()) {
            result.addError("Question text is required.");
        }
        if (optA == null || optA.trim().isEmpty()) result.addError("Option A cannot be empty.");
        if (optB == null || optB.trim().isEmpty()) result.addError("Option B cannot be empty.");
        if (optC == null || optC.trim().isEmpty()) result.addError("Option C cannot be empty.");
        if (optD == null || optD.trim().isEmpty()) result.addError("Option D cannot be empty.");

        if (correctOpt == null || !correctOpt.matches("^[ABCD]$")) {
            result.addError("A valid correct option (A, B, C, or D) must be selected.");
        }

        return result;
    }

    /**
     * Validates exam creation parameters.
     */
    public static ValidationResult validateExam(String title, int durationMinutes, int totalQuestions, double totalMarks, double passPercentage) {
        ValidationResult result = new ValidationResult();

        if (title == null || title.trim().isEmpty()) {
            result.addError("Exam title cannot be empty.");
        }
        if (durationMinutes <= 0) {
            result.addError("Duration must be greater than 0 minutes.");
        }
        if (totalQuestions <= 0) {
            result.addError("Total questions must be greater than 0.");
        }
        if (totalMarks <= 0) {
            result.addError("Total marks must be greater than 0.");
        }
        if (passPercentage < 0 || passPercentage > 100) {
            result.addError("Passing percentage must be between 0 and 100.");
        }

        return result;
    }
}
