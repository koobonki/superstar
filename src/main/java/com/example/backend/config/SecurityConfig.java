package com.example.backend.config;

import com.example.backend.service.ComUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 전체 설정
 * - 어떤 URL을 누가 접근할 수 있는지
 * - 로그인/로그아웃 동작
 * - 비밀번호 비교 방식
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ComUserDetailsService comUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
            // 인증 처리할 provider 등록
            .authenticationProvider(daoAuthenticationProvider)

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 로그인 페이지와 에러 페이지는 누구나 접근 가능
                .requestMatchers("/login", "/error").permitAll()

                // Swagger 관련 URL은 USER 또는 ADMIN만 접근 가능
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                .hasAnyRole("USER", "ADMIN")

                // 세션 정보 조회 API도 로그인 사용자만 접근 가능
                .requestMatchers("/getSessionInfo")
                .hasAnyRole("USER", "ADMIN")

                // 일반 API도 USER/ADMIN 권한 필요
                .requestMatchers("/api/**")
                .hasAnyRole("USER", "ADMIN")

                // 나머지 요청도 인증 필요
                .anyRequest().authenticated()
            )

            // 폼 로그인 사용
            .formLogin(form -> form
                // 로그인 성공 후 Swagger로 이동
                .defaultSuccessUrl("/swagger-ui.html", true)
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // 사용자 조회 서비스 연결
        provider.setUserDetailsService(comUserDetailsService);

        // 비밀번호 비교 로직 연결
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

        // 커스텀 PasswordEncoder
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                // 저장할 때는 BCrypt 해시 생성
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }

                // DB 값이 BCrypt 형태면 BCrypt 비교
                if (encodedPassword.startsWith("$2a$")
                        || encodedPassword.startsWith("$2b$")
                        || encodedPassword.startsWith("$2y$")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }

                // 이행 단계 호환: 평문 비교 (운영에서는 제거 권장)
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}
