package com.example.backend.controller;

import com.example.backend.security.ComUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 응답 JSON DTO
     * 예) {"empno":"admin","name":"관리자"}
     */
    public record SessionInfoResponse(String empno, String name) {
    }
}
