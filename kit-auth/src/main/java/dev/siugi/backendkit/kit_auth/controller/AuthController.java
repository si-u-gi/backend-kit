package dev.siugi.backendkit.kit_auth.controller;

import dev.siugi.backendkit.kit_auth.controller.dto.LoginRequest;
import dev.siugi.backendkit.kit_auth.controller.dto.SignupRequest;
import dev.siugi.backendkit.kit_auth.service.AuthService;
import dev.siugi.backendkit.kit_auth.user.AuthUser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnBean(AuthService.class)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> signup(
//            @RequestBody SignupRequest request
//    ) {
//        try {
//            AuthUser user = authService.signup(
//                    request.email(),
//                    request.password()
//            );
//
//            return ResponseEntity
//                    .status(HttpStatus.CREATED)
//                    .body(Map.of(
//                            "id", user.id(),
//                            "email", user.email()
//                    ));
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(
//            @RequestBody LoginRequest request
//    ) {
//        try {
//            return ResponseEntity.ok(
//                    authService.login(
//                            request.email(),
//                            request.password()
//                    )
//            );
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }
}