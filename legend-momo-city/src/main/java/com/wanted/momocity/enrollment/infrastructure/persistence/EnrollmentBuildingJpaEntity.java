package com.wanted.momocity.enrollment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "building")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 생성하지 못하게 protected로 제한
public class EnrollmentBuildingJpaEntity {

    // building 테이블의 기본키
    @Id
    private Long id;

    // 건물 소유자 Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 건물 카테고리
    @Column(name = "category", nullable = false)
    private String category;

    // 건물 위치
    @Column(name = "position", nullable = false)
    private Long position;

    // 건물 레벨
    @Column(name = "level", nullable = false)
    private Integer level;
}
