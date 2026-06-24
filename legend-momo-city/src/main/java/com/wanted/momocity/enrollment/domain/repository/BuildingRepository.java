package com.wanted.momocity.enrollment.domain.repository;

import com.wanted.momocity.enrollment.domain.model.Building;

import java.util.List;

public interface BuildingRepository {
    List<Building> findByUserId(Long userId);
}
