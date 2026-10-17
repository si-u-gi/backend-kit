
package dev.siugi.backendkit.kit_auth.service;

import dev.siugi.backendkit.kit_auth.jwt.JwtService;
import dev.siugi.backendkit.kit_auth.password.PasswordHashService;
import dev.siugi.backendkit.kit_auth.user.AuthUser;
import dev.siugi.backendkit.kit_auth.user.AuthUserService;

import java.util.Map;

public class AuthService {
    private final AuthUserService authUserService;
    private final PasswordHashService passwordHashService;
    private final JwtService jwtService;

    public AuthService(
            AuthUserService authUserService,
            PasswordHashService passwordHashService,
            JwtService jwtService
    ) {
        this.authUserService = authUserService;
        this.passwordHashService = passwordHashService;
        this.jwtService = jwtService;
    }

    public AuthUser signup(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "이메일을 입력해 주세요."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "비밀번호를 입력해 주세요."
            );
        }

        if (authUserService.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "이미 가입된 이메일입니다."
            );
        }

        String passwordHash = passwordHashService.hash(password);

        return authUserService.createUser(email, passwordHash);
    }

    public Map<String, String> login(String email, String password) {
        AuthUser user = authUserService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "이메일 또는 비밀번호가 올바르지 않습니다."
                ));

        if (!passwordHashService.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        String accessToken = jwtService.generateAccessToken(user.id());

        return Map.of("accessToken", accessToken, "tokenType", "Bearer");
    }
}
