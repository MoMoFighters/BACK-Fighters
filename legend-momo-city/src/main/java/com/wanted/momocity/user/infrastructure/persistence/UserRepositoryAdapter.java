package com.wanted.momocity.user.infrastructure.persistence;

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
