package com.example.backend.controller;

import com.example.backend.security.ComUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 샘플 API 컨트롤러
 */
@RestController
public class SampleController {

    /**
     * 현재 로그인 세션에서 empno, name 조회
     * GET /getSessionInfo
     */
    @GetMapping("/getSessionInfo")
    public ResponseEntity<SessionInfoResponse> getSessionInfo(Authentication authentication) {
        // 인증 정보가 없거나 익명 사용자면 401
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // principal에 우리가 만든 ComUserPrincipal이 들어있으면 empno/name 반환
        Object principal = authentication.getPrincipal();
        if (principal instanceof ComUserPrincipal user) {
            return ResponseEntity.ok(new SessionInfoResponse(user.getEmpno(), user.getName()));
        }

        // 혹시 다른 principal 타입인 경우 fallback
        String username = authentication.getName();
        return ResponseEntity.ok(new SessionInfoResponse(username, username));
    }

    /**
     * OAuth2 로그인 세션 정보 조회용 API
     * - Swagger Authorize(OAuth2) 완료 후 호출
     */
    @Operation(
            summary = "현재 OAuth2 로그인 세션 조회",
            description = "OAuth2 로그인 후 세션/사용자/권한 정보 조회",
            security = @SecurityRequirement(name = "oauth2")
    )
    @GetMapping("/api/session/me")
    public ResponseEntity<OAuthSessionInfoResponse> getOAuthSessionInfo(Authentication authentication,
                                                                        HttpSession session) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String empno;
        String name;
        Object principal = authentication.getPrincipal();

        if (principal instanceof ComUserPrincipal user) {
            empno = user.getEmpno();
            name = user.getName();
        } else {
            // OAuth2 로그인 사용자면 NameIdentifier를 사용하고, 없으면 인증 이름 사용
            empno = authentication.getName();
            name = authentication.getName();
        }

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(new OAuthSessionInfoResponse(
                session.getId(),
                empno,
                name,
                roles
        ));
    }

    /**
     * 응답 JSON DTO
     * 예) {"empno":"admin","name":"관리자"}
     */
    public record SessionInfoResponse(String empno, String name) {
    }

    /**
     * OAuth2 세션 조회 응답 DTO
     */
    public record OAuthSessionInfoResponse(String sessionId, String empno, String name, List<String> roles) {
    }
}
