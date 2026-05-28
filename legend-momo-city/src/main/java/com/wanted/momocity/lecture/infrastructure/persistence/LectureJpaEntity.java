package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "lecture")
public class LectureJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LectureCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LectureStatus status;

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

    /*
     * JPA Entity의 상태값을 변경
     * 트랜잭션 안에서 호출하면 dirty checking으로 DB에 반영
     */
    public void changeStatus(LectureStatus status) {
        this.status = status;
    }
}