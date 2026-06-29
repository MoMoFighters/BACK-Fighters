package com.wanted.momocity.admin.infrastructure.persistence;

import com.wanted.momocity.admin.domain.notice.AdminNotice;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notice")
@Getter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

/* comment.
    admin_notice DB 테이블 컬럼을 자바 필드로 표현한 클래스다.
    도메인 객체는 DB 를 모르기 때문에, 이 클래스가 toDomain() / fromDomain() 으로 도메인 <-> DB 번역 역할
 */

public class AdminNoticeJpaEntity {

    // 기본키, DB에서 자동 증가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VARCHAR(200) 컬럼
    private String title;

    // TEXT 컬럼
    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isPinned;

    // 생성 시각 — 한 번 저장 후 변경 불가
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // JPA 엔티티 → 도메인 객체로 변환
    public AdminNotice toDomain() {
        return AdminNotice.restore(id, title, content, isPinned, createdAt, updatedAt);
    }

    // 도메인 객체 → JPA 엔티티로 변환 (저장용)
    public static AdminNoticeJpaEntity fromDomain(AdminNotice notice) {
        return AdminNoticeJpaEntity.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .isPinned(notice.isPinned())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

}
