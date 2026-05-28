package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.domain.model.Category;

import java.time.LocalDate;

public interface UserQueryUsecase {

    UserDetailView userDetail(Long userId);

    void checkNickname(String nickname);

    RenderingBuildingsView userBuildingInfo(Long userId);

    record UserDetailView(
            String profileImageUrl,
            String email,
            String name,
            String nickname,
            LocalDate birth
    ){}

    record RenderingBuildingsView(
            Category category,
            Long position,
            Integer level
    ){}
}
