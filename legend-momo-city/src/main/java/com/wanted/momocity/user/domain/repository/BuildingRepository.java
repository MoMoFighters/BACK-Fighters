package com.wanted.momocity.user.domain.repository;

import com.wanted.momocity.user.domain.model.Building;

import java.util.Optional;

public interface BuildingRepository {
    Optional<Building> findByUserId(Long userId);
}
