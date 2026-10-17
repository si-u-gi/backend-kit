package dev.siugi.backendkit.kit_auth.controller.dto;

public record SignupRequest(
        String email,
        String password
) {
}