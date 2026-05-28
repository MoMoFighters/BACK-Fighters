package com.wanted.momocity.user.application.usecase;

import java.time.LocalDate;

public interface UserQueryUsecase {

    UserDetailView userDetail(Long userId);

    record UserDetailView(
            String profileImageUrl,
            String email,
            String name,
            String nickname,
            LocalDate birth
    ){}
}
