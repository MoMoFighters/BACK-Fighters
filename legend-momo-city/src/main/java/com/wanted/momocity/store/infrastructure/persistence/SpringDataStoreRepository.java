package com.wanted.momocity.store.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataStoreRepository extends JpaRepository<StoreJpaEntity,Long> {

    // 상품 이름으로 id 찾기
    Optional<StoreJpaEntity> findByName(String name);
}
