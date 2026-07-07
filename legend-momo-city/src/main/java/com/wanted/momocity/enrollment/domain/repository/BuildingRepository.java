package com.wanted.momocity.enrollment.domain.repository;

import com.wanted.momocity.enrollment.domain.model.Building;
import com.wanted.momocity.global.domain.model.Category;

import java.util.List;

public interface BuildingRepository {
    List<Building> findByUserId(Long userId);

    // 사용자 Id와 카테고리롤 건물 존재 여부 확인
    boolean existsByUserIdAndCategory(Long userId, Category category);

    // 건물 저장
    Building save(Building building);

    // 사용자 Id와 건물 위치로 해당 위치레 이미 건물이 존재하는지 확인
    boolean existsByUserIdAndPosition(Long userId, Long position);
}
