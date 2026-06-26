package com.wanted.momocity.store.infrastructure.persistence;

import com.wanted.momocity.store.domain.model.CheckIsOrderedResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataStoreRepository extends JpaRepository<StoreJpaEntity,Long> {

    // 상품 이름으로 id 찾기
    Optional<StoreJpaEntity> findByName(String name);

    // 상품 이름으로 id/url찾기
    @Query("SELECT new com.wanted.momocity.store.domain.model.CheckIsOrderedResult(s.id, s.url) FROM StoreJpaEntity s WHERE s.name = :name")
    Optional<CheckIsOrderedResult> findIdAndUrlByName(@Param("name") String itemName);


    // 상품 이름으로 url 찾기
//    @Query("SELECT s.url FROM StoreJpaEntity s WHERE s.name = :name")
//    String findUrlByName(@Param("name") String name);

}
