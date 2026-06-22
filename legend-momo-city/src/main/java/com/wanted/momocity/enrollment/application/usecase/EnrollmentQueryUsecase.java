package com.wanted.momocity.enrollment.application.usecase;

import com.wanted.momocity.global.domain.model.Category;

import java.util.List;

public interface EnrollmentQueryUsecase {
    // 해당 메서드가 사용되는 경우는 두 가지
    // 1. 메인페이지 랜더링 시
    // 2. 마이페이지에서 건물 정보 렌더링 시
    List<RenderingBuildingsView> userBuildingInfo(Long userId);

    record RenderingBuildingsView(
            Category category,
            Long position,
            Integer level
    ){}

}
