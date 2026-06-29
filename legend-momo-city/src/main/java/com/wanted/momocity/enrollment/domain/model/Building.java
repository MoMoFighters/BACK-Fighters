package com.wanted.momocity.enrollment.domain.model;

import com.wanted.momocity.global.domain.model.Category;

import java.time.LocalDateTime;

public class Building {
    private final Long id;
    private final Long userId;
    private final Category category;
    private final Long position;
    private final Integer level;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Building(Long id, Long userId, Category category, Long position, Integer level, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.position = position;
        this.level = level;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 새 건물 도메인
    public static Building create(
            Long userId,
            Category category,
            Long position
    ) {
        return new Building(
                null, // 신규 생성이라 아직 ID는 없음
                userId,
                category,
                position,
                1, // 모든 새 건물은 레벨 1로 생성
                // create와 update는 DB 저장 시 생성
                null,
                null
        );
    }

    public Long getUserId() {
        return userId;
    }

    public Category getCategory() {
        return category;
    }

    public Long getPosition() {
        return position;
    }

    public Integer getLevel() {
        return level;
    }
}
