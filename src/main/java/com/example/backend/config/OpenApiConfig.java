package com.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 기본 설명 설정
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Backend API")
                        .version("v1")
                        .description("""
                                Swagger 접근 시 Spring Security 로그인 필요.
                                인증 정보는 tb_com_user(user_id, password, role) 기반으로 검증됩니다.
                                로그인 후 /getSessionInfo에서 현재 세션 사용자(empno, name)를 확인할 수 있습니다.
                                """));
    }
}
