# 전체 소스 설명 및 작업지시서 (OAuth2 기준)

## 1) 현재 Git 반영 소스 목록 (branch: `cursor/swagger-session-info-0e07`)

### 루트
- `README.md`

### 문서
- `docs/SESSION_TEST_SCENARIO.md`
- `docs/SOURCE_FULL_AND_WORK_INSTRUCTIONS.md` (본 문서)

### 스크립트
- `scripts/test-session-info.sh`

### Java 소스
- `src/main/java/com/example/backend/config/OpenApiConfig.java`
- `src/main/java/com/example/backend/config/SecurityConfig.java`
- `src/main/java/com/example/backend/controller/SampleController.java`
- `src/main/java/com/example/backend/domain/ComUser.java`
- `src/main/java/com/example/backend/repository/jpa/ComUserRepository.java`
- `src/main/java/com/example/backend/security/ComUserPrincipal.java`
- `src/main/java/com/example/backend/service/ComUserDetailsService.java`

### 설정
- `src/main/resources/application.yml`

---

## 2) 전체 소스 (현재 반영본)

> 참고: 아래 내용은 현재 Git에 반영된 기준으로 정리한 원문입니다.

### 2-1. `src/main/java/com/example/backend/config/SecurityConfig.java`

```java
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
```

### 2-2. `src/main/java/com/example/backend/config/OpenApiConfig.java`

```java
package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 기본 설명 설정
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme oauth2Scheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("OAuth2 Authorization Code with OIDC")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://auth.example.com/oauth2/authorize")
                                .tokenUrl("https://auth.example.com/oauth2/token")
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID Connect scope")
                                        .addString("profile", "Profile scope")
                                        .addString("email", "Email scope"))));

        return new OpenAPI()
                .info(new Info()
                        .title("Backend API")
                        .version("v1")
                        .description("""
                                OAuth2(OIDC) 로그인 기반 API 문서입니다.
                                Swagger의 Authorize 버튼으로 로그인 후 API를 호출할 수 있습니다.
                                """))
                .components(new Components()
                        .addSecuritySchemes("oauth2", oauth2Scheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList("oauth2"));
    }
}
```

### 2-3. `src/main/java/com/example/backend/controller/SampleController.java`

```java
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
```

### 2-4. `src/main/java/com/example/backend/domain/ComUser.java`

```java
package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * tb_com_user 테이블과 매핑되는 엔티티 클래스
 * -> DB 한 행(row)을 Java 객체 하나로 다루기 위해 사용
 */
@Entity
@Table(name = "tb_com_user")
@Getter
@Setter
@NoArgsConstructor
public class ComUser {

    // PK 컬럼(ID)
    @Id
    @Column(name = "ID")
    private Long id;

    // 로그인 아이디(user_id)
    @Column(name = "user_id", nullable = false, length = 50, unique = true)
    private String userId;

    // 비밀번호(BCrypt 해시 권장)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // 사용자 이름
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    // 권한 값 (ROLE_ADMIN / ROLE_USER 등)
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    // 생성 시간
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### 2-5. `src/main/java/com/example/backend/repository/jpa/ComUserRepository.java`

```java
package com.example.backend.repository.jpa;

import com.example.backend.domain.ComUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 조회용 JPA Repository
 * -> DB에서 ComUser를 읽을 때 사용
 */
public interface ComUserRepository extends JpaRepository<ComUser, Long> {

    /**
     * user_id로 사용자 1명을 조회
     * 없으면 Optional.empty() 반환
     */
    Optional<ComUser> findByUserId(String userId);
}
```

### 2-6. `src/main/java/com/example/backend/security/ComUserPrincipal.java`

```java
package com.example.backend.security;

import com.example.backend.domain.ComUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 로그인 후 Security 세션에 저장될 사용자 정보 클래스
 * 기본 User에 empno, name 정보를 추가로 담기 위해 만듦
 */
public class ComUserPrincipal extends User {

    // 사번처럼 사용할 값
    private final String empno;

    // 화면/응답에 보여줄 이름
    private final String name;

    public ComUserPrincipal(ComUser user, Collection<? extends GrantedAuthority> authorities) {
        // 부모(User)에는 username, password, authorities를 넣음
        super(user.getUserId(), user.getPassword(), authorities);

        // tb_com_user에 empno 컬럼이 없으므로 user_id를 empno처럼 사용
        this.empno = user.getUserId();

        // 사용자 이름
        this.name = user.getUserName();
    }

    public String getEmpno() {
        return empno;
    }

    public String getName() {
        return name;
    }
}
```

### 2-7. `src/main/java/com/example/backend/service/ComUserDetailsService.java`

```java
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
```

### 2-8. `src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  security:
    oauth2:
      client:
        registration:
          my-oidc:
            client-id: your-client-id
            client-secret: your-client-secret
            scope:
              - openid
              - profile
              - email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-name: My OIDC
        provider:
          my-oidc:
            issuer-uri: https://your-idp.example.com/realms/your-realm

springdoc:
  swagger-ui:
    oauth:
      client-id: your-client-id
      use-pkce-with-authorization-code-grant: true
```

### 2-9. `docs/SESSION_TEST_SCENARIO.md`

```md
# Swagger OAuth2 로그인 후 session API 테스트

## 1) 사전 설정

- `src/main/resources/application.yml`의 OAuth2 값 실제값으로 변경
  - `spring.security.oauth2.client.registration.my-oidc.client-id`
  - `spring.security.oauth2.client.registration.my-oidc.client-secret`
  - `spring.security.oauth2.client.provider.my-oidc.issuer-uri`
- `src/main/java/com/example/backend/config/OpenApiConfig.java`의 아래 값 실제값으로 변경
  - `authorizationUrl`
  - `tokenUrl`
- IdP(통합 로그인 서버)에 Redirect URI 등록
  - `http://localhost:8080/login/oauth2/code/my-oidc`

## 2) 브라우저 테스트

1. `http://localhost:8080/swagger-ui.html` 접속
2. 우측 상단 `Authorize` 클릭
3. OAuth2 로그인 수행(IdP 로그인 화면 진입/인증/동의)
4. 인증 완료 후 Swagger 복귀
5. `/api/session/me` 실행

예상 응답:
```json
{
  "sessionId": "A1B2C3...",
  "empno": "login-id",
  "name": "login-id",
  "roles": ["ROLE_USER"]
}
```

## 3) 오류 점검 포인트

- Authorize 후에도 401 발생
  - `issuer-uri`, `authorizationUrl`, `tokenUrl` 설정 불일치 확인
- OAuth2 로그인 페이지 미노출
  - 클라이언트 등록(`client-id`, `redirect-uri`) 점검
- 403 발생
  - 권한 매핑(`roles`) 및 메서드 보안 어노테이션 점검
```

### 2-10. `scripts/test-session-info.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail

# ----------------------------------------
# 사용법:
# BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh <bearer_token>
# 예:
# BASE_URL=http://localhost:8080 ./scripts/test-session-info.sh eyJhbGciOi...
# ----------------------------------------

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${1:-}"

if [[ -z "$TOKEN" ]]; then
  echo "Usage: BASE_URL=http://localhost:8080 $0 <bearer_token>"
  exit 1
fi

echo "[1] GET /api/session/me with Bearer token"
curl -fsS "${BASE_URL}/api/session/me" \
  -H "Authorization: Bearer ${TOKEN}"
echo
```

---

## 3) 동작 오류 체크 결과

### 3-1. 이번 턴에서 수정한 오류/불일치

1. **테스트 스크립트 불일치**
   - 기존: 폼 로그인(`/login`) + CSRF 기준
   - 현재 코드: OAuth2/OIDC 기준
   - 조치: `scripts/test-session-info.sh`를 OAuth2 Bearer 토큰 호출 방식으로 수정

2. **테스트 문서 불일치**
   - 기존: `/getSessionInfo` 폼 로그인 시나리오
   - 현재 코드: Swagger OAuth2 `Authorize` 시나리오
   - 조치: `docs/SESSION_TEST_SCENARIO.md`를 OAuth2 기준으로 전면 정리

### 3-2. 현재 남아 있는 리스크(필수 확인)

아래 항목은 코드상 placeholder 상태이므로 실제값으로 바꾸지 않으면 실행 실패합니다.

- `OpenApiConfig.java`
  - `authorizationUrl("https://auth.example.com/oauth2/authorize")`
  - `tokenUrl("https://auth.example.com/oauth2/token")`
- `application.yml`
  - `client-id`, `client-secret`
  - `issuer-uri`

---

## 4) 작업지시서 (실제 적용 절차)

### Step 1. OAuth2 설정값 실값 반영

1. `src/main/resources/application.yml` 수정
   - `client-id`, `client-secret`, `issuer-uri` 실값 입력
2. `src/main/java/com/example/backend/config/OpenApiConfig.java` 수정
   - `authorizationUrl`, `tokenUrl` 실값 입력

### Step 2. IdP(통합 로그인 서버) 설정

1. Client 생성/수정
2. Redirect URI 등록
   - `http://localhost:8080/login/oauth2/code/my-oidc`
3. 필요 scope 허용
   - `openid profile email`

### Step 3. 런타임 라이브러리 확인

프로젝트에 아래 dependency가 있어야 합니다.

- `spring-boot-starter-oauth2-client`
- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-security`
- `springdoc-openapi-starter-webmvc-ui` (Swagger 사용 시)

### Step 4. 실행 및 기능 검증

1. 서버 실행
2. `/swagger-ui.html` 접속
3. `Authorize` 클릭 후 OAuth2 로그인
4. `/api/session/me` 실행
5. 응답의 `sessionId`, `empno`, `roles` 확인

### Step 5. 장애 시 확인 순서

1. 401: `issuer-uri`, token 검증 설정 확인
2. 403: role 매핑/권한 어노테이션 확인
3. 리다이렉트 오류: Redirect URI 불일치 확인
4. Swagger 로그인 실패: `authorizationUrl`, `tokenUrl` 확인

---

## 5) 요약

- 현재 소스는 **OAuth2(OIDC) + Swagger Authorize** 구조로 정리됨
- 테스트 문서/스크립트도 OAuth2 기준으로 맞춤
- 실제 구동 성공 여부는 IdP 실환경 값 반영과 dependency 유무에 의해 결정됨
