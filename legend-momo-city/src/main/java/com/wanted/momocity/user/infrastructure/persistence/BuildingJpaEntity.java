package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.global.infrastructure.persistence.BaseTimeEntity;
import com.wanted.momocity.user.domain.model.Category;
import jakarta.persistence.*;

@Entity
@Table(name = "building")
public class BuildingJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Long position;

    @Column(nullable = false)
    private Integer level;

    public Long getId() {
        return id;
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
