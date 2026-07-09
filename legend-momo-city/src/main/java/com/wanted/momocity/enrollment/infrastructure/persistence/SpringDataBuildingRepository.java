package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.global.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingJpaEntity, Long> {
    List<BuildingJpaEntity> findByUserId(Long userId);
    // 사용자의 특정 카테고리 건물이 존재하는지 확인
    boolean existsByUserIdAndCategory(Long userId, Category category);

    // DB에서 user_id와 position이 일치하는 건물 엔티티를 조회
    Optional<BuildingJpaEntity> findByUserIdAndPosition(Long userId, Long position);
}
