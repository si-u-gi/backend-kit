package dev.siugi.backendkit.kit_auth.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashService {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(
            String rawPassword,
            String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
