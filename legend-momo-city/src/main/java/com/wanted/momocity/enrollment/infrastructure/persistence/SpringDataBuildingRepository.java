package com.wanted.momocity.enrollment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingJpaEntity, Long> {
    List<BuildingJpaEntity> findByUserId(Long userId);
}
