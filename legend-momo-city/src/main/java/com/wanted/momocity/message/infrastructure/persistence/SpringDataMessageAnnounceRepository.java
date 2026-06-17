package com.wanted.momocity.message.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMessageAnnounceRepository extends JpaRepository<MessageAnnounceJpaEntity, Long> {
}
