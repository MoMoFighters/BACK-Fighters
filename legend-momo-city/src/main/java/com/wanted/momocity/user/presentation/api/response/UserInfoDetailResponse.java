package com.wanted.momocity.user.presentation.api.response;

import com.wanted.momocity.enrollment.application.usecase.EnrollmentQueryUsecase;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;

import java.util.List;

public record UserInfoDetailResponse(
        UserQueryUsecase.UserDetailView userDetail,
        List<EnrollmentQueryUsecase.RenderingBuildingsView> buildings) {
}
