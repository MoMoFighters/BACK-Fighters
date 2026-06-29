package com.wanted.momocity.user.presentation.api.response;

import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.domain.model.BuildingInfo;

import java.util.List;

public record UserInfoDetailResponse(
        UserQueryUsecase.UserDetailView userDetail,
        List<BuildingInfo> buildings) {
}
