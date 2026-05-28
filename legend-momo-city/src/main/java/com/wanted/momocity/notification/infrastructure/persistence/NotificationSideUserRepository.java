package com.wanted.momocity.notification.infrastructure.persistence;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSideUserRepository extends JpaRepository<UserWithFMJpaEntity, Long> {
}
