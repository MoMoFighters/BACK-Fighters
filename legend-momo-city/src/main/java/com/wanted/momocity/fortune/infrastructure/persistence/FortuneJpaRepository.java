package com.wanted.momocity.fortune.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FortuneJpaRepository extends JpaRepository<FortuneJpaEntity, Long> {
    @Query(
            // MySQL의 RAND 함수로 운세 순서를 무작위로 섞고 한 건만 가져온다.
            value = "SELECT * FROM fortunes ORDER BY RAND() LIMIT 1",
            nativeQuery = true
    )

        // 366개의 운세 중 무작위 운세 한 건을 조회
    Optional<FortuneJpaEntity> findRandom();
}
