package dev.siugi.backendkit.kit_auth.controller.dto;

public record LoginRequest(
        String email,
        String password
) {
}