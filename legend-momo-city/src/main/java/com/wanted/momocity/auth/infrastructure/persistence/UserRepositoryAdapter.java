package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.port.UpdatePasswordPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository("authUserRepositoryAdapter")
@Transactional
public class UserRepositoryAdapter implements UserRepository, LoadUserPort, UpdatePasswordPort {
    // UserRepository → 회원가입 관련 (저장, 중복확인)
    // LoadUserPort   → 인증 관련 (유저 조회)


    // 순수한 자바 객체를 엔티티 객체로 만듦

    private final SpringDataAuthUserRepository springDataAuthUserRepository;

    public UserRepositoryAdapter(SpringDataAuthUserRepository springDataAuthUserRepository) {
        this.springDataAuthUserRepository = springDataAuthUserRepository;
    }

    @Override
    public User register(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                null,                    // nickname
                null,                    // birth
                user.getProfileImageUrl(),                    // profileImageUrl
                user.getRole(),
                user.getStatus(),
                user.getCategory(),
                user.getProof(),
                user.getPoint(),                       // point
                false,                   // isPaid
                false,                   // doNotDisturb
                user.getCreatedAt(),         // createdAt
                LocalDateTime.now(),     // updatedAt
                null,                    // deletedAt
                user.getIsTempPwd()                    // isTempPwd
        );

        UserJpaEntity saved = springDataAuthUserRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataAuthUserRepository.existsByEmail(email);
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
        return springDataAuthUserRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataAuthUserRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public void updatePassword(String email, String encodedPassword) {
        springDataAuthUserRepository.updatePassword(email, encodedPassword);
    }
}
