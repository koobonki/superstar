package com.example.backend.service;

import com.example.backend.domain.ComUser;
import com.example.backend.repository.jpa.ComUserRepository;
import com.example.backend.security.ComUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 로그인 시 Spring Security가 호출하는 서비스
 * -> 입력받은 user_id로 DB에서 사용자 조회 후 UserDetails 반환
 */
@Service
@RequiredArgsConstructor
public class ComUserDetailsService implements UserDetailsService {

    private final ComUserRepository comUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 1) DB에서 사용자 조회
        ComUser user = comUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        // 2) role 문자열을 ROLE_* 형태로 정리
        String normalizedRole = normalizeRole(user.getRole());

        // 3) Security 권한 객체 생성
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(normalizedRole));

        // 4) 세션에 저장할 Principal 반환 (empno/name 포함)
        return new ComUserPrincipal(user, authorities);
    }

    /**
     * role 문자열을 안전하게 ROLE_* 형태로 통일
     * - ADMIN / ROLE_ADMIN -> ROLE_ADMIN
     * - USER / ROLE_USER   -> ROLE_USER
     * - 그 외/빈값          -> ROLE_USER(기본)
     */
    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        String normalized = role.trim().toUpperCase();
        if ("ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized)) {
            return "ROLE_ADMIN";
        }
        if ("USER".equals(normalized) || "ROLE_USER".equals(normalized)) {
            return "ROLE_USER";
        }
        return "ROLE_USER";
    }
}
