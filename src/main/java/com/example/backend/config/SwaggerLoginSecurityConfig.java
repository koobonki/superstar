package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Swagger 로그인 전용 보안 설정
 * - DB를 사용하지 않고 소스 코드에 계정을 고정한다.
 * - 운영에서는 반드시 외부 비밀 저장소/DB 인증으로 교체한다.
 */
@Configuration
public class SwaggerLoginSecurityConfig {

    // 요구사항: 프로그램 소스에 직접 ID/PW 정의
    private static final String SWAGGER_LOGIN_ID = "swagger_admin";
    private static final String SWAGGER_LOGIN_PW = "swagger1234!";

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(SWAGGER_LOGIN_ID)
                        // {noop}: 데모용 평문 비밀번호 (운영 사용 금지)
                        .password("{noop}" + SWAGGER_LOGIN_PW)
                        .roles("SWAGGER")
                        .build()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Swagger 화면/문서는 로그인 필요
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                        .hasRole("SWAGGER")
                        // 그 외 URL은 현재 요구사항 범위 밖이므로 허용
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        // 로그인 성공 시 Swagger UI로 이동
                        .defaultSuccessUrl("/swagger-ui.html", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }
}
