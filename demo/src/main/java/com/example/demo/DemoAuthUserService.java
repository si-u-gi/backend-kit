package com.example.demo;

import dev.siugi.backendkit.kit_auth.user.AuthUser;
import dev.siugi.backendkit.kit_auth.user.AuthUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DemoAuthUserService implements AuthUserService {

    private final ConcurrentMap<String, AuthUser> users =
            new ConcurrentHashMap<>();

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(email.trim().toLowerCase()));
    }

    @Override
    public AuthUser createUser(String email, String passwordHash) {
        String key = email.trim().toLowerCase();

        AuthUser user = new AuthUser(
                UUID.randomUUID().toString(),
                email.trim(),
                passwordHash
        );

        AuthUser existing = users.putIfAbsent(key, user);
        if (existing != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        return user;
    }
}
