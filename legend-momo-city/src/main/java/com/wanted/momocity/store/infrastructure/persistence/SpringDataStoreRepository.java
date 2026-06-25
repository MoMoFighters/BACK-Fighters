package com.wanted.momocity.store.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStoreRepository extends JpaRepository<StoreJpaEntity,Long> {
}
