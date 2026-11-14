package com.qveto.exam;

import com.qveto.exam.validation.InputValidator;
import com.qveto.exam.validation.ValidationResult;
import static com.qveto.exam.Assert.*;

public class InputValidatorTest {

    public void testEmailValidation() {
        assertTrue(InputValidator.isValidEmail("admin@qveto.com"));
        assertTrue(InputValidator.isValidEmail("student.1@university.edu"));
        assertFalse(InputValidator.isValidEmail("invalid-email"));
        assertFalse(InputValidator.isValidEmail("missing@domain"));
        assertFalse(InputValidator.isValidEmail(null));
    }

    public void testPasswordValidation() {
        assertTrue(InputValidator.isValidPassword("Admin@123"));
        assertTrue(InputValidator.isValidPassword("123456"));
        assertFalse(InputValidator.isValidPassword("12345"));
        assertFalse(InputValidator.isValidPassword(""));
        assertFalse(InputValidator.isValidPassword(null));
    }

    public void testRegistrationValidationSuccess() {
        ValidationResult result = InputValidator.validateRegistration(
                "Aarav Patel", "aarav@example.com", "Password123", "Password123"
        );
        assertTrue(result.isValid(), "Registration should be valid");
    }

    public void testRegistrationPasswordMismatch() {
        ValidationResult result = InputValidator.validateRegistration(
                "Aarav Patel", "aarav@example.com", "Password123", "Mismatch456"
        );
        assertFalse(result.isValid());
        assertTrue(result.getFirstError().contains("Passwords do not match"));
    }

    public void testRegistrationEmptyFields() {
        ValidationResult result = InputValidator.validateRegistration(
                "", "", "", ""
        );
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 3);
    }

    public void testQuestionValidation() {
        ValidationResult valid = InputValidator.validateQuestion(
                "Java", "What is JVM?", "Machine", "Compiler", "OS", "Browser", "A"
        );
        assertTrue(valid.isValid());

        ValidationResult invalid = InputValidator.validateQuestion(
                "Java", "What is JVM?", "Machine", "", "OS", "Browser", "X"
        );
        assertFalse(invalid.isValid());
    }
}
