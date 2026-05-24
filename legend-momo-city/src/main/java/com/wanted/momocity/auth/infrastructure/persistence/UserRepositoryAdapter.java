package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Transactional
public class UserRepositoryAdapter implements UserRepository, LoadUserPort {

    // 순수한 자바 객체를 엔티티 객체로 만듦

    private final SpringDataUserRepository springDataUserRepository;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User register(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                null,                    // nickname
                null,                    // birth
                null,                    // profileImageUrl
                user.getRole(),
                user.getStatus(),
                user.getCategory(),
                user.getProof(),
                0,                       // point
                false,                   // isPaid
                false,                   // doNotDisturb
                Instant.now(),           // createdAt
                LocalDateTime.now(),     // updatedAt
                null,                    // deletedAt
                false                    // isTempPwd
        );

        UserJpaEntity saved = springDataUserRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }


    private User toDomain(UserJpaEntity entity){
        return User.restore(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getName(),
                entity.getNickname(),
                entity.getBirth(),
                entity.getProfileImageUrl(),
                entity.getRole(),
                entity.getStatus(),
                entity.getCategory(),
                entity.getProof(),
                entity.getPoint(),
                entity.isPaid(),
                entity.isDoNotDisturb(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.isTempPwd()
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(this::toDomain);
    }
}
