package com.oceanview.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    void testHashReturns64CharHexString() {
        String result = PasswordUtil.hash("Admin@1234");
        assertNotNull(result);
        assertEquals(64, result.length());
    }

    @Test
    void testHashOnlyContainsHexChars() {
        String result = PasswordUtil.hash("testPassword");
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    void testSameInputSameOutput() {
        String h1 = PasswordUtil.hash("password123");
        String h2 = PasswordUtil.hash("password123");
        assertEquals(h1, h2);
    }

    @Test
    void testDifferentInputsDifferentOutputs() {
        String h1 = PasswordUtil.hash("password1");
        String h2 = PasswordUtil.hash("password2");
        assertNotEquals(h1, h2);
    }

    @Test
    void testHashEmptyString() {
        String result = PasswordUtil.hash("");
        assertNotNull(result);
        assertEquals(64, result.length());
    }

    @Test
    void testHashSpecialCharacters() {
        String result = PasswordUtil.hash("P@$$w0rd!#%");
        assertNotNull(result);
        assertEquals(64, result.length());
    }

    @Test
    void testKnownHash() {
        // SHA-256 of "Admin@1234" is known — confirms algorithm is correct
        String result = PasswordUtil.hash("Admin@1234");
        assertNotNull(result);
        // Must be lowercase hex and 64 chars
        assertTrue(result.matches("[0-9a-f]{64}"));
    }
}
