package com.wanted.momocity.user.domain.model;

import com.wanted.momocity.global.domain.model.Category;

public record BuildingInfo(
        Category category,
        Long position,
        Integer level,
        String buildingUrl
) {
}
