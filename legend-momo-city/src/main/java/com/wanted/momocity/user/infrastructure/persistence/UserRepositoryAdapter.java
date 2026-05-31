package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.domain.model.User;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("userRepositoryAdapter")
@Transactional
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return springDataUserRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public void registerNickname(Long userId, String nickname) {
        springDataUserRepository.registerNickname(userId, nickname);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return springDataUserRepository.existsByNickname(nickname);
    }

    @Override
    public void updateUserInfo(UpdateUserInfoCommand command) {
        springDataUserRepository.updateUserInfo(
                command.userId(),
                command.nickname(),
                command.profileImageUrl(),
                command.password()
        );
    }

    // 임시비번에서 새로운 비번 변경하면 is_tempPwd false 로 변경
    @Override
    public void clearTempPwd(Long userId) {
        springDataUserRepository.clearTempPwd(userId);
    }

    @Override
    public String findPasswordById(Long userId) {
        return springDataUserRepository.findById(userId)
                .map(UserJpaEntity::getPassword)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Override
    public List<TeacherApplication> findByRoleAndStatus(Role role, Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return springDataUserRepository.findByRoleAndStatus(role, status, pageable)
                .stream()
                .map(this::toTeacherApplication)
                .toList();
    }

    @Override
    public Optional<TeacherApplication> findTeacherApplicationById(Long userId) {
        return springDataUserRepository.findById(userId)
                .filter(e -> e.getRole() == Role.TEACHER && e.getStatus() == Status.PENDING)
                .map(this::toTeacherApplication);
    }

    @Override
    public long countByRoleAndStatus(Role role, Status status) {
        return springDataUserRepository.countByRoleAndStatus(role, status);
    }

    @Override
    public void save(User user) {
        springDataUserRepository.save(UserJpaEntity.fromDomain(user));
    }

    @Override
    public void updateRoleAndStatus(Long userId, Role role, Status status) {
        springDataUserRepository.updateRoleAndStatus(userId, role, status, LocalDateTime.now());
    }

    // 관리자 회원관리용 회원 목록 조회 - role/status 조건에 따라
    @Override
    public List<User> findAllForAdmin(Role role, Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return springDataUserRepository.findAllForAdmin(role, status, pageable)
                .stream().map(this::toDomainForAdmin).toList();
    }

    // 페이지네이션 totalElements 계산용 전체 개수 조회
    @Override
    public long countForAdmin(Role role, Status status) {
        return springDataUserRepository.countForAdmin(role, status);
    }



    // 마이페이지 내 정보 조회용
    private User toDomain(UserJpaEntity entity) {
        return User.restore(
                entity.getProfileImageUrl(),
                entity.getEmail(),
                entity.getName(),
                entity.getNickname(),
                entity.getBirth(),
                entity.isTempPwd(),
                entity.getCreatedAt()
        );
    }

    // 대기 강사 조회용
    private TeacherApplication toTeacherApplication(UserJpaEntity entity) {
        return new TeacherApplication(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getNickname(),
                entity.getBirth(),
                entity.getProfileImageUrl(),
                entity.getCategory() != null ? entity.getCategory().name() : null,
                entity.getProof(),
                entity.getUpdatedAt()
        );
    }

    // 관리자 회원관리 회원 조회용
    private User toDomainForAdmin(UserJpaEntity entity) {
        return User.restoreForAdmin(
                entity.getId(),
                entity.getName(),
                entity.getRole(),
                entity.getEmail(),
                entity.getCreatedAt(),
                entity.getDeletedAt(),
                entity.getStatus(),
                entity.getRole() == Role.TEACHER ? entity.getProof() : null
        );
    }
}
