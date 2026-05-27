package com.wanted.momocity.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserOauthRepository extends JpaRepository<UserOauthJpaEntity, Long> {
    Optional<UserOauthJpaEntity> findByProviderAndProviderId(String provider, String providerId);

}
