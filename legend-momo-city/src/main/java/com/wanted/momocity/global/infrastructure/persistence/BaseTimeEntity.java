package com.wanted.momocity.global.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/*
 * BaseTimeEntity의 역할 — 한 줄 요약
 * "모든 엔티티의 created_at / updated_at 컬럼을 자동으로 채워주는 공통 부모 클래스"
 *
 * 클린 아키텍처 위치:
 * - 도메인 모델은 JPA를 모른다.
 * - 각 컨텍스트의 infrastructure/persistence 패키지에 있는 XxxJpaEntity가 이 클래스를 상속한다.
 * - 도메인 ↔ JpaEntity 변환은 RepositoryAdapter가 담당한다.
 *
 * deleted_at(soft delete) 컬럼이 필요한 엔티티는
 * 별도 BaseSoftDeleteEntity를 정의하거나 각 JpaEntity에서 직접 박는다.
 *
 * 동작 조건:
 * - 메인 애플리케이션 또는 Config 클래스에 @EnableJpaAuditing 이 적용되어 있어야 한다.
 *   (JpaAuditingConfig 가 그 역할을 한다)
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
