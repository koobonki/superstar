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
