package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
import com.wanted.momocity.user.domain.model.User;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public String findPasswordById(Long userId) {
        return springDataUserRepository.findById(userId)
                .map(UserJpaEntity::getPassword)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    private User toDomain(UserJpaEntity entity) {
        return User.restore(
                entity.getProfileImageUrl(),
                entity.getEmail(),
                entity.getName(),
                entity.getNickname(),
                entity.getBirth()
        );
    }
}
