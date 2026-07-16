package com.wanted.momocity.fortune.domain.model;

import java.time.LocalDateTime;

public class ForTune {

    // 운세 id (1~366)
    private Long id;

    // 운세 내용
    private String content;

    // 운세 유형 (GOOD, NATURAL, BAD)
    private FortuneTone tone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private ForTune() {
    }

    // DB에서 조회한 운세 데이터를 도메인 객체로 복원
    public static ForTune reconstitute (
            Long id,
            String content,
            FortuneTone tone,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        ForTune fortune = new ForTune();

        fortune.id = id;
        fortune.content=content;
        fortune.tone=tone;
        fortune.createdAt=createdAt;
        fortune.updatedAt=updatedAt;

        return fortune;
    }

    // 운세 ID를 반환
    public Long getId() {
        return id;
    }

    // 운세 문구를 반환
    public String getContent() {
        return content;
    }

    // 운세 유형을 반환
    public FortuneTone getTone() {
        return tone;
    }

    // 운세 데이터의 생성 시각을 반환
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // 운세 데이터의 수정 시각을 반환
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
