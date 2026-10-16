package dev.siugi.backendkit.kit_auth.auth;

import dev.siugi.backendkit.kit_auth.entity.AuthUser;
import dev.siugi.backendkit.kit_auth.repository.AuthUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 이메일입니다."
            );
        }

        AuthUser user = new AuthUser(
                email,
                passwordEncoder.encode(request.password()),
                request.name().trim()
        );

        AuthUser savedUser = userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", savedUser.getId(),
                        "email", savedUser.getEmail(),
                        "name", savedUser.getName(),
                        "message", "회원가입이 완료되었습니다."
                ));
    }
}
