package com.qveto.exam;

import com.qveto.exam.util.PasswordUtil;
import static com.qveto.exam.Assert.*;

public class PasswordUtilTest {

    public void testHashNotNullOrEmpty() {
        String hash = PasswordUtil.hash("Secret123");
        assertNotNull(hash);
        assertEquals(64, hash.length(), "SHA-256 hash must be 64 hexadecimal characters");
    }

    public void testHashConsistency() {
        String hash1 = PasswordUtil.hash("Admin@123");
        String hash2 = PasswordUtil.hash("Admin@123");
        assertEquals(hash1, hash2, "Identical input must produce identical hash");
    }

    public void testVerifySuccess() {
        String hash = PasswordUtil.hash("Student@123");
        assertTrue(PasswordUtil.verify("Student@123", hash));
    }

    public void testVerifyFailure() {
        String hash = PasswordUtil.hash("Student@123");
        assertFalse(PasswordUtil.verify("WrongPassword", hash));
    }

    public void testNullInput() {
        assertEquals("", PasswordUtil.hash(null));
        assertFalse(PasswordUtil.verify(null, "somehash"));
    }
}
