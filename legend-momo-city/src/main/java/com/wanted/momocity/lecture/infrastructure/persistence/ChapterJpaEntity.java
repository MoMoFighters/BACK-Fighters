package com.wanted.momocity.lecture.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.VideoStatus;
import jakarta.persistence.*;

// ChapterJpaEntity는 chapter 테이블과 매핑되는 JPA Entity입니다.
// 도메인 모델이 아니라 DB 저장용 객체입니다.
@Entity
@Table(name = "chapter")
public class ChapterJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 강의에 소속된 챕터인지 나타냅니다.
    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    // 챕터 제목입니다.
    @Column(nullable = false, length = 200)
    private String title;

    // 강의 내 챕터 노출 순서입니다.
    @Column(name = "order_no", nullable = false)
    private int orderNo;

    // 동영상 S3 URL입니다. 챕터 등록 시점에는 null일 수 있습니다.
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    // 동영상 파일 크기입니다. 동영상 등록 전에는 null일 수 있습니다.
    @Column(name = "video_size_bytes")
    private Long videoSizeBytes;

    // 동영상 재생 시간입니다. 인코딩 전에는 null일 수 있습니다.
    @Column(name = "duration_sec")
    private Integer durationSec;

    // 동영상 처리 상태입니다.
    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false, length = 30)
    private VideoStatus videoStatus;

    // 원본 동영상 파일명입니다. 동영상 등록 전에는 null일 수 있습니다.
    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    protected ChapterJpaEntity() {
    }

    private ChapterJpaEntity(
            Long id,
            Long lectureId,
            String title,
            int orderNo,
            String videoUrl,
            Long videoSizeBytes,
            Integer durationSec,
            VideoStatus videoStatus,
            String originalFilename
    ) {
        this.id = id;
        this.lectureId = lectureId;
        this.title = title;
        this.orderNo = orderNo;
        this.videoUrl = videoUrl;
        this.videoSizeBytes = videoSizeBytes;
        this.durationSec = durationSec;
        this.videoStatus = videoStatus;
        this.originalFilename = originalFilename;
    }

    // 도메인 모델을 JPA Entity로 변환합니다.
    public static ChapterJpaEntity from(LectureChapter chapter) {
        return new ChapterJpaEntity(
                chapter.getId(),
                chapter.getLectureId(),
                chapter.getTitle(),
                chapter.getOrderNo(),
                chapter.getVideoUrl(),
                chapter.getVideoSizeBytes(),
                chapter.getDurationSec(),
                chapter.getVideoStatus(),
                chapter.getOriginalFilename()
        );
    }

    // JPA Entity를 도메인 모델로 복원합니다.
    public LectureChapter toDomain() {
        return LectureChapter.restore(
                id,
                lectureId,
                title,
                orderNo,
                videoUrl,
                videoSizeBytes,
                durationSec,
                videoStatus,
                originalFilename,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public String getTitle() {
        return title;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public Long getVideoSizeBytes() {
        return videoSizeBytes;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public VideoStatus getVideoStatus() {
        return videoStatus;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }
}