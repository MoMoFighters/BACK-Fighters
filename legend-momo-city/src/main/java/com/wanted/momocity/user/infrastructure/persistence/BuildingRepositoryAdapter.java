package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.user.domain.model.Building;
import com.wanted.momocity.user.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BuildingRepositoryAdapter implements BuildingRepository {

    private final SpringDataBuildingRepository springDataBuildingRepository;

    @Override
    public Optional<Building> findByUserId(Long userId) {
        return springDataBuildingRepository.findByUserId(userId)
                .map(entity -> new Building(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getCategory(),
                        entity.getPosition(),
                        entity.getLevel(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ));
    }


}
