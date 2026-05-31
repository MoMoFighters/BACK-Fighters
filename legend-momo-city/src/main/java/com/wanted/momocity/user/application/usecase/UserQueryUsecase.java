package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.domain.model.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserQueryUsecase {

    UserDetailView userDetail(Long userId);

    void checkNickname(String nickname);

    RenderingBuildingsView userBuildingInfo(Long userId);

    record UserDetailView(
            String profileImageUrl,
            String email,
            String name,
            String nickname,
            LocalDate birth,
            Boolean isTempPwd,
            LocalDateTime createdAt

    ){}

    record RenderingBuildingsView(
            Category category,
            Long position,
            Integer level
    ){}


    // 승인 대기 중인 강사 전체 목록 조회
    TeacherApplicationListResult getApplicationList(int page, int size);

    // 승인 대기 중인 강사 목록 상세
    TeacherApplication getApplicationDetail(Long userId);

    record TeacherApplicationListResult(
            List<TeacherApplication> applications,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    // 관리자 회원관리용 사용자 조회
    AdminUserListResult getAdminUserList(String role, String status, int page, int size);

    record AdminUserListResult(
            List<?> users,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}
}
