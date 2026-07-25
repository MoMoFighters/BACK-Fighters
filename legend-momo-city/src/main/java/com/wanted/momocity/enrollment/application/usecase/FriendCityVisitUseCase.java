package com.wanted.momocity.enrollment.application.usecase;

public interface FriendCityVisitUseCase {
    // 로그인 사용자가 targetUserId 사용자의 도시를 방문하고 도시 정보를 반환
    EnrollmentQueryUsecase.FriendBuildingsView visitFriendCity(
            Long loginUserId,
            Long targetUserId
    );
}
