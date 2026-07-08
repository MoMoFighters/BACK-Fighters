package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.enrollment.domain.model.Building;
import com.wanted.momocity.enrollment.domain.repository.BuildingRepository;
import com.wanted.momocity.global.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BuildingRepositoryAdapter implements BuildingRepository {

    private final SpringDataBuildingRepository springDataBuildingRepository;

    // 중복 변환 방지
    private Building toDomain(BuildingJpaEntity entity) {
        return new Building(
                entity.getId(),
                entity.getUserId(),
                entity.getCategory(),
                entity.getPosition(),
                entity.getLevel(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<Building> findByUserId(Long userId) {
        return springDataBuildingRepository.findByUserId(userId)
                .stream()
                .map(this::toDomain
                )
                .toList();
    }

    @Override
    public boolean existsByUserIdAndCategory(Long userId, Category category) {
        return springDataBuildingRepository.existsByUserIdAndCategory(userId, category); // JPA repository에 존재 여부 조회 위임
    }

    @Override
    public Building save(Building building) {
        BuildingJpaEntity entity = BuildingJpaEntity.from(building); // 도메인 모델을 JPA 엔티티로 변환
        BuildingJpaEntity savedEntity = springDataBuildingRepository.saveAndFlush(entity); // DB에 즉시 반영해서 유니크 제약 위반을 이 시점에 발생시킴
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Building> findByUserIdAndPosition(Long userId, Long position) {
        return springDataBuildingRepository.findByUserIdAndPosition(userId, position).map(this::toDomain);
    }
}
