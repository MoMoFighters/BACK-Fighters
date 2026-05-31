package com.wanted.momocity.user.presentation.api.response;

import java.time.LocalDateTime;

public class AdminUserListResponse {

    // 학생 조회 / 기본 전체 조회
    public record Default(
            Long id,
            String name,
            String role,
            String email,
            LocalDateTime createdAt,
            String status
    ) {}

    // 강사조회
    public record Teacher(
            Long id,
            String name,
            String role,
            String email,
            LocalDateTime createdAt,
            String status,
            String proof
    ) {}

    // 탈퇴한 사용자 조회
    public record Deleted(
            Long id,
            String name,
            String role,
            String email,
            LocalDateTime deletedAt
    ) {}
}
