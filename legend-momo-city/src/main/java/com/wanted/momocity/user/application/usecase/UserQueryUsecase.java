package com.wanted.momocity.user.application.usecase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public interface UserQueryUsecase {

    UserDetailView userDetail(Long userId);

    void checkNickname(String nickname);

    record UserDetailView(
            String profileImageUrl,
            String email,
            String name,
            String nickname,
            LocalDate birth
    ){}
}
