package com.wanted.momocity.fortune.domain.repository;

import com.wanted.momocity.fortune.domain.model.Fortune;

import java.util.Optional;

public interface FortuneRepository {

    // 운세 Id로 운세 한 건 조회
    Optional<Fortune> findById(
            Long fortuneId
    );

    // 366개의 운세 중 무작위 운세 한 건을 조회
    Optional<Fortune> findRandom();
}
