package com.wanted.momocity.global.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * OpenApiConfig의 역할 — 한 줄 요약
 * "Swagger / OpenAPI 문서 메타정보와 JWT Bearer 인증 스키마를 정의한다."
 *
 * Swagger UI 의 "Authorize" 버튼에 JWT 토큰을 입력하면
 * 보호된 API 를 브라우저에서 바로 테스트할 수 있다.
 *
 * 인증 담당이 JwtAuthenticationFilter 를 붙이면 즉시 활용 가능.
 *
 * 위치: global/infrastructure/config
 * (문서화는 비즈니스 규칙과 무관한 기술 설정이다.)
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI momocityOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Legend Momo City API")
                        .version("v1")
                        .description("학습 플랫폼 momocity 의 REST API 문서"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme));
    }
}
