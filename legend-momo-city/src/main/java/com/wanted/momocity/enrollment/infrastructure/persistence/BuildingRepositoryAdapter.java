package com.wanted.momocity.enrollment.infrastructure.persistence;

import com.wanted.momocity.enrollment.domain.model.Building;
import com.wanted.momocity.enrollment.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BuildingRepositoryAdapter implements BuildingRepository {

    private final SpringDataBuildingRepository springDataBuildingRepository;

    @Override
    public List<Building> findByUserId(Long userId) {
        return springDataBuildingRepository.findByUserId(userId)
                .stream()
                .map(entity -> new Building(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getCategory(),
                        entity.getPosition(),
                        entity.getLevel(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList();
    }


}
