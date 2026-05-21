package com.wanted.momocity.global.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
 * JpaAuditingConfig의 역할 — 한 줄 요약
 * "BaseTimeEntity의 @CreatedDate / @LastModifiedDate 가 자동으로 채워지도록 JPA Auditing을 활성화한다."
 *
 * 메인 애플리케이션 클래스에 @EnableJpaAuditing 을 박지 않고
 * 별도 Config 클래스로 분리한 이유:
 * - 테스트에서 Auditing 동작을 끄고 싶을 때 이 Config만 제외하면 된다.
 * - 메인 클래스가 책임을 너무 많이 떠안지 않도록 설정 책임을 분리한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
