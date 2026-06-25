package com.wanted.momocity.user.presentation.api.response;

import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;

import java.time.LocalDateTime;

public class AdminUserListResponse {

    // 기본 회원 조회
    public record Default(
            Long id,
            String name,
            Role role,
            String email,
            LocalDateTime createdAt,
            Status status,
            Long suspensionCount,
            LocalDateTime suspendedUntil
    ) {}

    // 탈퇴한 사용자 조회
    public record Deleted(
            Long id,
            String name,
            Role role,
            String email,
            LocalDateTime deletedAt,
            Status status
    ) {}
}
