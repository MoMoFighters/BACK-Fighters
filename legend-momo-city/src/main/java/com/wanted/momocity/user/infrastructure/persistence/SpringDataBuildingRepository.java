package com.wanted.momocity.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingJpaEntity, Long> {
    Optional<BuildingJpaEntity> findByUserId(Long userId);
}
