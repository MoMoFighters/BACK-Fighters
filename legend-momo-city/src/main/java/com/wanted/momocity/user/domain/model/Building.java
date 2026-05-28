package com.wanted.momocity.user.domain.model;

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
