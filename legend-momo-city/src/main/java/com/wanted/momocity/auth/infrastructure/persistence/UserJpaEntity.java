package com.wanted.momocity.auth.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
@Table(name="user")
public class UserJpaEntity {

    // 직접 user 테이블을 다루는 엔티티 클래스


}
