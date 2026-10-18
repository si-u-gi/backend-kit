package dev.siugi.backendkit.kit_auth.config;

import dev.siugi.backendkit.kit_auth.jwt.JwtAuthenticationFilter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import jakarta.servlet.DispatcherType;

@AutoConfiguration(after = KitAuthAutoConfiguration.class)
@EnableConfigurationProperties(KitAuthProperties.class)
public class KitAuthSecurityAutoConfiguration {

    @Bean
    @ConditionalOnBean(JwtAuthenticationFilter.class)
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain kitAuthSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            KitAuthProperties properties
    ) throws Exception {

        KitAuthProperties.Csrf csrfProperties =
                properties.getCsrf();

        if (!csrfProperties.isEnabled()) {
            http.csrf(csrf -> csrf.disable());
        } else {
            http.csrf(csrf -> {
                if (csrfProperties.isCookieBased()) {
                    csrf.csrfTokenRepository(
                            CookieCsrfTokenRepository.withHttpOnlyFalse()
                    );
                } else {
                    csrf.csrfTokenRepository(
                            new HttpSessionCsrfTokenRepository()
                    );
                }

                if (!csrfProperties.getIgnoredPaths().isEmpty()) {
                    csrf.ignoringRequestMatchers(
                            csrfProperties.getIgnoredPaths()
                                    .toArray(String[]::new)
                    );
                }
            });
        }

        http
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, exception) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value())
                ))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> {

                    // 내부 오류 처리 요청 허용
                    auth.dispatcherTypeMatchers(
                            DispatcherType.ERROR,
                            DispatcherType.FORWARD
                    ).permitAll();

                    // kit-auth가 기본 제공하는 로그인/회원가입 화면
                    auth.requestMatchers(
                            HttpMethod.GET,
                            "/login",
                            "/signup",
                            "/kit-auth/**"
                    ).permitAll();

                    // 사이트별 공개 페이지
                    String[] publicPaths = properties
                            .getSecurity()
                            .getPublicPaths()
                            .toArray(String[]::new);

                    if (publicPaths.length > 0) {
                        auth.requestMatchers(publicPaths).permitAll();
                    }

                    // CSRF 토큰 발급 API
                    auth.requestMatchers(
                            HttpMethod.GET,
                            "/api/auth/csrf"
                    ).permitAll();

                    // 로그인 / 회원가입 API
                    auth.requestMatchers(
                            HttpMethod.POST,
                            "/api/auth/login",
                            "/api/auth/signup"
                    ).permitAll();

                    // 그 외 요청은 인증 필요
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}