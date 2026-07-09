package com.wanted.momocity.user.application.usecase;

import com.wanted.momocity.user.domain.model.BuildingInfo;
import com.wanted.momocity.user.domain.model.Membership;
import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.presentation.api.response.AdminUserDetailResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserQueryUsecase {

    UserDetailResult userDetail(Long userId);

    record UserDetailResult(
            UserDetailView userDetail,
            List<BuildingInfo> buildings
    ) {}

    void checkNickname(String nickname);

    AdminUserDetailResponse getUserDetail(Long userId);

    record UserDetailView(
            String profileImageUrl,
            String email,
            String name,
            Long point,
            Boolean doNotDisturb,
            Membership membership,
            LocalDateTime membershipStart,
            String nickname,
            Boolean isTempPwd,
            LocalDateTime createdAt

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
