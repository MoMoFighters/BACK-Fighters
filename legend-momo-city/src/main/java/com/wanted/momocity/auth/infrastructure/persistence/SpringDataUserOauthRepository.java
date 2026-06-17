package com.wanted.momocity.auth.infrastructure.persistence;

import com.wanted.momocity.auth.domain.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserOauthRepository extends JpaRepository<UserOauthJpaEntity, Long> {
    Optional<UserOauthJpaEntity> findByProviderAndProviderId(Provider provider, String providerId);

}
