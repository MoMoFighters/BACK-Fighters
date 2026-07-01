package com.wanted.momocity.user.application.service;

import com.wanted.momocity.global.application.s3.S3PresignedUrlPort;
import com.wanted.momocity.user.application.port.GetUserBuildingsPort;
import com.wanted.momocity.user.application.port.UserReportListPort;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.domain.model.*;
import com.wanted.momocity.user.domain.repository.UserRepository;
import com.wanted.momocity.user.presentation.api.response.AdminUserDetailResponse;
import com.wanted.momocity.user.presentation.api.response.AdminUserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUsecase {

    private final UserRepository userRepository;
    private final UserPolicy userPolicy;
    private final S3PresignedUrlPort s3PresignedUrlPort;
    private final UserReportListPort userReportListPort;
    private final GetUserBuildingsPort getUserBuildingsPort;

    // 마이페이지 내 정보 조회용
    @Override
    public UserDetailResult userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("사용자를 찾을 수 없습니다."));

        UserDetailView userDetail = new UserDetailView(
                user.getProfileImageUrl(), user.getEmail(), user.getName(),
                user.getPoint(), user.getNickname(), user.getTempPwd(), user.getCreatedAt()
        );

        List<BuildingInfo> buildings =
                getUserBuildingsPort.getUserBuildings(userId);

        return new UserDetailResult(userDetail, buildings);
    }

    @Override
    public void checkNickname(String nickname) {
        userPolicy.nicknamePolicy(nickname);
    }

    // 관리자가 조회할 회원 1명의 정보
    @Override
    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<ReportInfo> reports = userReportListPort.getReportsByUserId(userId); // 신고내역 가져오기

        return new AdminUserDetailResponse(user.getEmail(),user.getRole(),user.getCreatedAt()
                ,user.getCategory(),user.getName(),user.getSuspensionCount()
                ,reports);
    }


    // 대기 강사 전체 조회
    @Override
    public TeacherApplicationListResult getApplicationList(int page, int size) {
        List<TeacherApplication> list = userRepository.findByRoleAndStatus(Role.TEACHER, Status.PENDING, page, size);
        long totalElements = userRepository.countByRoleAndStatus(Role.TEACHER, Status.PENDING);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new TeacherApplicationListResult(list, page, size, totalElements, totalPages);
    }

    // 대기 강사 상세 조회
    @Override
    public TeacherApplication getApplicationDetail(Long userId) {

        TeacherApplication app = userRepository.findTeacherApplicationById(userId)
                .orElseThrow(()-> new UserNotFoundException("해당 강사 신청자를 찾을 수 없습니다."));

        // presignedUrl 생성
        String presignedUrl = s3PresignedUrlPort.generatePresignedUrl(app.proof());
        return app.withPresignedUrl(presignedUrl);
    }

    // 관리자 회원관리용 회원 조회
    @Override
    @Cacheable(
            value = "adminUserList",
            key = "'page:' + #page + ':size:' + #size", // 키 예시 : page:1:size:10
            condition = "#role == null && #status == null")
    // role과 status 파라미터가 둘 다 null일 때만 캐싱 로직이 동작
    // = 전체 회원조회 때만 캐시 적용
    public AdminUserListResult getAdminUserList(String role, String status, int page, int size) {
        Role roleEnum = role != null ? Role.valueOf(role) : null;
        Status statusEnum = status != null ? Status.valueOf(status) : null;

        List<?> list = userRepository.findAllForAdmin(roleEnum, statusEnum, page, size)
                .stream()
                .map(user -> {
                    if (statusEnum == Status.DELETED) {
                        return new AdminUserListResponse.Deleted(
                                user.getId(), user.getName(), user.getRole(), user.getEmail(), user.getDeletedAt(), user.getStatus());
                    } else {
                        return new AdminUserListResponse.Default(
                                user.getId(), user.getName(), user.getRole(), user.getEmail(),
                                user.getCreatedAt(), user.getStatus(), user.getSuspensionCount(),user.getSuspendedUntil());
                    }
                })
                .toList();

        long totalElements = userRepository.countForAdmin(roleEnum, statusEnum);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new AdminUserListResult(list, page, size, totalElements, totalPages);
    }
}