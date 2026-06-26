package com.wanted.momocity.store.infrastructure.persistence;

import com.wanted.momocity.store.domain.model.Type;
import com.wanted.momocity.store.domain.model.CheckIsOrderedResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataStoreRepository extends JpaRepository<StoreJpaEntity,Long> {

    // 상품 이름으로 id 찾기
    Optional<StoreJpaEntity> findByName(String name);

    // profile 타입의 전체 목록 찾기
    // 추후에 profile말고 다른 type이 생기게 될 경우를 고려한 작업
    List<StoreJpaEntity> findAllByType(Type type);
    // 상품 이름으로 id/url찾기
    @Query("SELECT new com.wanted.momocity.store.domain.model.CheckIsOrderedResult(s.id, s.url) FROM StoreJpaEntity s WHERE s.name = :name")
    Optional<CheckIsOrderedResult> findIdAndUrlByName(@Param("name") String itemName);


    // 상품 이름으로 url 찾기
//    @Query("SELECT s.url FROM StoreJpaEntity s WHERE s.name = :name")
//    String findUrlByName(@Param("name") String name);

}

