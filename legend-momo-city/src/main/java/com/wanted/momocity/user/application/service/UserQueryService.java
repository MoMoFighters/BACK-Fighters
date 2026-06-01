package com.wanted.momocity.user.application.service;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.domain.model.User;
import com.wanted.momocity.user.domain.repository.BuildingRepository;
import com.wanted.momocity.user.domain.repository.UserRepository;
import com.wanted.momocity.user.presentation.api.response.AdminUserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQueryUsecase {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final UserPolicy userPolicy;


    // 마이페이지 내 정보 조회용
    @Override
    public UserDetailView userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new DomainRuleViolationException("사용자를 찾을 수 없습니다."));

        return new UserDetailView(
                user.getProfileImageUrl(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getBirth(),
                user.getTempPwd(),
                user.getCreatedAt()
        );
    }

    @Override
    public void checkNickname(String nickname) {
        userPolicy.nicknamePolicy(nickname);
    }

    @Override
    public RenderingBuildingsView userBuildingInfo(Long userId) {
        return buildingRepository.findByUserId(userId)
                .map(building -> new RenderingBuildingsView(
                        building.getCategory(),
                        building.getPosition(),
                        building.getLevel()
                ))
                .orElse(new RenderingBuildingsView(null, null, null));
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
        return userRepository.findTeacherApplicationById(userId)
                .orElseThrow(() -> new DomainRuleViolationException("해당 강사 신청자를 찾을 수 없습니다."));
    }

    // 관리자 회원관리용 회원 조회
    @Override
    public AdminUserListResult getAdminUserList(String role, String status, int page, int size) {
        // role이랑 status가 컨트롤러에서 String으로 넘어와서 서비스에서 enum 타입으로 변환
        Role roleEnum = role != null ? Role.valueOf(role) : null;
        Status statusEnum = status != null ? Status.valueOf(status) : null;

        List<?> list = userRepository.findAllForAdmin(roleEnum, statusEnum, page, size)
                .stream()
                .map(user -> {
                    if (statusEnum == Status.DELETED) {
                        return new AdminUserListResponse.Deleted(
                                user.getId(), user.getName(), user.getRole().name(), user.getEmail(), user.getDeletedAt());
                    } else if (user.getRole() == Role.TEACHER) {
                        return new AdminUserListResponse.Teacher(
                                user.getId(), user.getName(), user.getRole().name(), user.getEmail(),
                                user.getCreatedAt(), user.getStatus().name(), user.getProof());
                    } else {
                        return new AdminUserListResponse.Default(
                                user.getId(), user.getName(), user.getRole().name(), user.getEmail(),
                                user.getCreatedAt(), user.getStatus().name());
                    }
                })
                .toList();

        long totalElements = userRepository.countForAdmin(roleEnum, statusEnum);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new AdminUserListResult(list, page, size, totalElements, totalPages);
    }
}
