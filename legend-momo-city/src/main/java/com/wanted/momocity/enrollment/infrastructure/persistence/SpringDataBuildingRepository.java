package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.global.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingJpaEntity, Long> {
    List<BuildingJpaEntity> findByUserId(Long userId);
    // 사용자의 특정 카테고리 건물이 존재하는지 확인
    boolean existsByUserIdAndCategory(Long userId, Category category);

    // 사용자의 특정 위치에 건물이 있는지 확인
    boolean existsByUserIdAndPosition(Long userId, Long position);
}
