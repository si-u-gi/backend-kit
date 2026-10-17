package dev.siugi.backendkit.kit_auth.config;

import dev.siugi.backendkit.kit_auth.jwt.JwtAuthenticationFilter;
import dev.siugi.backendkit.kit_auth.jwt.JwtService;
import dev.siugi.backendkit.kit_auth.password.PasswordHashService;

import dev.siugi.backendkit.kit_auth.service.AuthService;
import dev.siugi.backendkit.kit_auth.user.AuthUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class KitAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordHashService passwordHashService() {
        return new PasswordHashService();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "kit.auth.jwt",
            name = "secret"
    )
    @ConditionalOnMissingBean
    public JwtService jwtService(
            @Value("${kit.auth.jwt.secret}") String secret,
            @Value("${kit.auth.jwt.access-token-expiration-ms:3600000}")
            long expirationMs) {

        return new JwtService(secret, expirationMs);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "kit.auth.jwt",
            name = "secret"
    )
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    @ConditionalOnBean({
            AuthUserService.class,
            JwtService.class,
            PasswordHashService.class
    })
    @ConditionalOnMissingBean
    public AuthService authService(
            AuthUserService authUserService,
            PasswordHashService passwordHashService,
            JwtService jwtService
    ) {
        return new AuthService(
                authUserService,
                passwordHashService,
                jwtService
        );
    }
}
