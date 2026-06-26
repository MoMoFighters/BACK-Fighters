package com.wanted.momocity.user.domain.model;

public record UpdateUserInfoData(
        Long userId,
        String profileImageUrl,
        String nickname,
        String password
) {
}
