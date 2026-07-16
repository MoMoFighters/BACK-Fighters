package com.wanted.momocity.enrollment.application.usecase;

import com.wanted.momocity.enrollment.application.query.EnrollmentQuery;
import com.wanted.momocity.enrollment.presentation.api.response.EnrollmentProgressResponse;
import com.wanted.momocity.global.domain.model.Category;

import java.util.List;

public interface EnrollmentQueryUsecase {
    // 해당 메서드가 사용되는 경우는 두 가지
    // 1. 메인페이지 랜더링 시
    // 2. 마이페이지에서 건물 정보 렌더링 시
    List<RenderingBuildingsView> userBuildingInfo(Long userId);

    // 친구 마을 건물 정보 조회하는 메서드
    FriendBuildingsView friendBuildingInfo(
            Long loginUserId,
            Long targetUserId
    );

    // 학습 진척도 조회 기능입니다.
    EnrollmentProgressResponse getProgress(
            EnrollmentQuery.GetEnrollmentProgressQuery query
    );

    // 친구 도시 주인의 닉네임과 건물 목록을 함께 전달하는 응답 객체
    record FriendBuildingsView(
            String nickname,
            List<RenderingBuildingsView> buildings
    ) {}

    record RenderingBuildingsView(
    Category category,
    Long position,
    Integer level,
    // 카테고리에 맞는 빌딩 이미지 생성
    String buildingUrl
    ){}



}
