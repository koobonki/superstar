package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2/OIDC 기반 인증 설정
 * - Swagger UI는 공개
 * - API는 인증 필요
 * - 로그인은 OAuth2 공급자(IdP)로 위임
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/session/me", "/getSessionInfo").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/swagger-ui.html", true)
                )
                // Swagger OAuth2 Authorize로 발급된 Bearer 토큰도 API에서 검증 가능하도록 설정
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .logout(logout -> logout.logoutSuccessUrl("/swagger-ui.html"));

        return http.build();
    }
}
