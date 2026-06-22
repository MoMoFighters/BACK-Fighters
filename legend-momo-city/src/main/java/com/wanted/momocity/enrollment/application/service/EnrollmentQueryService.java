package com.wanted.momocity.enrollment.application.service;

import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.enrollment.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EnrollmentQueryService implements EnrollmentQueryUsecase {

    private final BuildingRepository buildingRepository;

    @Override
    public List<RenderingBuildingsView> userBuildingInfo(Long userId) {
        return buildingRepository.findByUserId(userId)
                .stream()
                .map(building -> new RenderingBuildingsView(
                        building.getCategory(),
                        building.getPosition(),
                        building.getLevel()
                ))
                .toList();
    }

}
