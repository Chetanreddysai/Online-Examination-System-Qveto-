package com.qveto.exam;

import java.util.Objects;

/**
 * Lightweight test assertion framework enabling zero-dependency test execution
 * across both IDEs and command-line environments.
 */
public final class Assert {

    private Assert() {}

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message != null ? message : "Expected true but was false");
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, null);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message != null ? message : "Expected false but was true");
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, null);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError((message != null ? message + " - " : "") +
                    "Expected: <" + expected + "> but was: <" + actual + ">");
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new AssertionError(message != null ? message : "Expected non-null object");
        }
    }

    public static void assertNotNull(Object actual) {
        assertNotNull(actual, null);
    }

    public static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new AssertionError(message != null ? message : "Expected null but was: " + actual);
        }
    }

    public static void assertNull(Object actual) {
        assertNull(actual, null);
    }
}
