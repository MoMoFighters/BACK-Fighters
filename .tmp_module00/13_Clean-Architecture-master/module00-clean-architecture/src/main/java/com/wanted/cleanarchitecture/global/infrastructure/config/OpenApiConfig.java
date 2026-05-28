package com.wanted.cleanarchitecture.global.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/*
 * Swagger/OpenAPI 설정은 API 문서화를 위한 기술 설정이다.
 * Domain, Application 계층의 비즈니스 규칙과 무관하므로 global.infrastructure.config에 둔다.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Clean Architecture DDD Course API",
                version = "v1",
                description = "EventStorming 결과를 DDD Aggregate, Command, Domain Event, Bounded Context로 구현한 수업용 API"
        )
)
public class OpenApiConfig {
}
