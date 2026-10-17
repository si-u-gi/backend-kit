package dev.siugi.backendkit.kit_auth.password;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashServiceTest {

    private final PasswordHashService passwordHashService =
            new PasswordHashService();

    @Test
    void passwordCanBeHashedAndVerified() {
        String rawPassword = "testPassword123!";

        String hash = passwordHashService.hash(rawPassword);

        assertNotEquals(rawPassword, hash);
        assertTrue(
                passwordHashService.matches(rawPassword, hash)
        );
        assertFalse(
                passwordHashService.matches("wrongPassword", hash)
        );
    }
}