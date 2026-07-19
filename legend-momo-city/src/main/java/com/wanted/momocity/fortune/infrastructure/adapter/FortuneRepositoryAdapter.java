package com.wanted.momocity.fortune.infrastructure.adapter;

import com.wanted.momocity.fortune.domain.model.Fortune;
import com.wanted.momocity.fortune.domain.repository.FortuneRepository;
import com.wanted.momocity.fortune.infrastructure.persistence.FortuneJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
@RequiredArgsConstructor
public class FortuneRepositoryAdapter implements FortuneRepository {

    private final FortuneJpaRepository fortuneJpaRepository;


    @Override
    public Optional<Fortune> findById(Long fortuneId) {
        return fortuneJpaRepository.findById(fortuneId)
                .map(entity -> entity.toDomain());
    }

    @Override
    public Optional<Fortune> findRandom() {
        return fortuneJpaRepository.findRandom()
                .map(entity -> entity.toDomain());
    }
}
