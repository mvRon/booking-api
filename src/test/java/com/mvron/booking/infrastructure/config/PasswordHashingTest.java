package com.mvron.booking.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashingTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void sameRawPasswordProducesDifferentHashes() {
        String hash1 = encoder.encode("secret123");
        String hash2 = encoder.encode("secret123");

        System.out.println("hash1 = " + hash1);
        System.out.println("hash2 = " + hash2);
        System.out.println("length = " + hash1.length());

        assertNotEquals(hash1, hash2);
    }

    @Test
    void matchesAcceptsCorrectPasswordOnly() {
        String hash = encoder.encode("secret123");

        assertTrue(encoder.matches("secret123", hash));
        assertFalse(encoder.matches("wrong-password", hash));
    }

    @Test
    void hashDoesNotContainRawPassword() {
        String hash = encoder.encode("secret123");

        assertFalse(hash.contains("secret123"));
        assertEquals(60, hash.length());
    }
}