package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.model.UserOauth;
import com.wanted.momocity.auth.domain.repository.UserOauthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserOauthRepositoryAdapter implements UserOauthRepository {

    private final SpringDataAuthUserRepository springDataAuthUserRepository;
    private final SpringDataUserOauthRepository springDataUserOauthRepository;

    @Override
    public UserOauth save(UserOauth userOauth) {
        UserJpaEntity newSocialUserId = springDataAuthUserRepository.findById(userOauth.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserOauthJpaEntity entity = new UserOauthJpaEntity(
                newSocialUserId,
                userOauth.getProvider(),
                userOauth.getProviderId(),
                userOauth.getCreatedAt()
        );

        UserOauthJpaEntity saved = springDataUserOauthRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<UserOauth> findByProviderAndProviderId(String provider, String providerId) {
        return springDataUserOauthRepository.findByProviderAndProviderId(provider, providerId)
                .map(this::toDomain);
    }

    private UserOauth toDomain(UserOauthJpaEntity entity) {
        User user = User.restore(
                entity.getUser().getId(),
                entity.getUser().getEmail(),
                entity.getUser().getPassword(),
                entity.getUser().getName(),
                entity.getUser().getNickname(),
                entity.getUser().getBirth(),
                entity.getUser().getProfileImageUrl(),
                entity.getUser().getRole(),
                entity.getUser().getStatus(),
                entity.getUser().getCategory(),
                entity.getUser().getProof(),
                entity.getUser().getPoint(),
                entity.getUser().isPaid(),
                entity.getUser().isDoNotDisturb(),
                entity.getUser().getCreatedAt(),
                entity.getUser().getUpdatedAt(),
                entity.getUser().getDeletedAt(),
                entity.getUser().isTempPwd()
        );

        return UserOauth.restore(
                entity.getId(),
                user,
                entity.getProvider(),
                entity.getProviderId(),
                entity.getCreatedAt()
        );
    }
}
