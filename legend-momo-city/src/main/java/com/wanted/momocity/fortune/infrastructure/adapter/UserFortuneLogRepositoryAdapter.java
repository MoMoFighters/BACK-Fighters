package com.wanted.momocity.fortune.infrastructure.adapter;

import com.wanted.momocity.fortune.domain.model.UserFortuneLog;
import com.wanted.momocity.fortune.domain.repository.UserFortuneLogRepository;
import com.wanted.momocity.fortune.infrastructure.persistence.UserFortuneLogJpaEntity;
import com.wanted.momocity.fortune.infrastructure.persistence.UserFortuneLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFortuneLogRepositoryAdapter implements UserFortuneLogRepository {

    private final UserFortuneLogJpaRepository userFortuneLogJpaRepository;

    // 운세 기록 도메인 객체를 저장 가능 한 JPA Entity 변환
    @Override
    public Optional<UserFortuneLog> findByUserIdAndDrawnDate(Long userId, LocalDate drawnDate) {
        return userFortuneLogJpaRepository.findByUserIdAndDrawnDate(userId, drawnDate)
                .map(entity -> entity.toDomain());
    }

    // 변환된 JPA Entity를 user_fortune_logs 테이블에 저장
    @Override
    public UserFortuneLog save(UserFortuneLog userFortuneLog) {
        UserFortuneLogJpaEntity entity = UserFortuneLogJpaEntity.fromDomain(userFortuneLog);
        UserFortuneLogJpaEntity savedEntity = userFortuneLogJpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}
