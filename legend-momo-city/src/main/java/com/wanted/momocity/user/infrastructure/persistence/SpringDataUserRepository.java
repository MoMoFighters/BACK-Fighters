package com.wanted.momocity.user.infrastructure.persistence;

import com.wanted.momocity.auth.infrastructure.persistence.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
}
