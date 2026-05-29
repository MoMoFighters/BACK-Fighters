package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import jakarta.persistence.*;

// DB 저장 클래스
@Entity
@Table(name = "lecture")
public class LectureJpaEntity extends BaseTimeEntity {

    // lecture 테이블의 기본 키
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 강의를 등록한 강사의 user id
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    // 강의 제목
    @Column(nullable = false, length = 200)
    private String title;

    // 강의 설명
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // 강의 대표 이미지 URL
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // 강의 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LectureCategory category;

    // 강의 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LectureStatus status;

    // 수강 완료한 사용자 수
    @Column(name = "completed_user_count", nullable = false)
    private int completedUserCount;

    protected LectureJpaEntity() {
    }

    public LectureJpaEntity(
            Long teacherId,
            String title,
            String description,
            String thumbnailUrl,
            LectureCategory category,
            LectureStatus status
    ) {
        this.teacherId = teacherId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
        this.status = status;
        this.completedUserCount = 0;
    }

    public Long getId() {
        return id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public LectureCategory getCategory() {
        return category;
    }

    public LectureStatus getStatus() {
        return status;
    }

    public int getCompletedUserCount() {
        return completedUserCount;
    }

    // 강의 상태만 변경한다.
    // 현재 강의 검수 요청 또는 관리자 상태 변경 시 사용된다.
    public void changeStatus(LectureStatus status) {
        this.status = status;
    }
}