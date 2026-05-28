package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.auth.infrastructure.persistence.SpringDataAuthUserRepository;
import com.wanted.momocity.auth.infrastructure.persistence.UserJpaEntity;
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

    private final SpringDataAuthUserRepository springDataAuthUserRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return springDataAuthUserRepository.findById(userId).map(this::toDomain);
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
