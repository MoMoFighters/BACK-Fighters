package com.wanted.momocity.admin.infrastructure.persistence;

import com.wanted.momocity.admin.domain.audit.ErrorLevel;
import com.wanted.momocity.admin.domain.audit.ErrorLog;
import com.wanted.momocity.admin.domain.audit.ErrorLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/* comment.
    ErrorLogRepositoryAdapter 정리
    1. 역할 : 도메인 ErrorLogRepository 인터페이스를 JPA 로 구현하는 어댑터
    2. 위치 : 인프라 계층
    3. 핵심 책임
       - 도메인 ErrorLog <-> ErrorLogJpaEntity 변환
       - SpringDataErrorLogRepository 호출을 감싸 도메인이 인프라를 모르게 처리하는 방식
    4. WHY 의존 방향 (클린 아키텍처)
        - implements ErrorLogRepository (도메인 인터페이스) ← 인프라가 도메인 약속 지킴
        - SpringDataErrorLogRepository 주입 (인프라 → 인프라) ← 같은 계층
        - ErrorLog 사용 (인프라 → 도메인) ← OK
        - 도메인은 이 클래스의 존재를 모름 (인터페이스만 봄)
    5. ReportRepositoryAdapter 와의 차이
       → 같은 구조 (Repository + Spring Data + Adapter 3 파일 패턴)
       → 다른 점 :
            정렬 키, 필터 컬럼, 도메인 변환 시
 */
@Repository
@Transactional
public class ErrorLogRepositoryAdapter implements ErrorLogRepository {

    private final SpringDataErrorLogRepository repository;

    public ErrorLogRepositoryAdapter(SpringDataErrorLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public ErrorLog save(ErrorLog errorLog) {
        ErrorLogJpaEntity entity = toEntity(errorLog);
        ErrorLogJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErrorLog> findRecent(int limit) {
        return repository.findAllByOrderByOccurredAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErrorLog> findByLevel(ErrorLevel level, int limit) {
        return repository.findAllByLevelOrderByOccurredAtDesc(level.name(), PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // === 변환 메서드 (도메인 ↔ 엔티티) ===

    // 도메인 → 엔티티 (신규 저장 시 id=null, JPA 가 auto-increment 부여)
    private ErrorLogJpaEntity toEntity(ErrorLog errorLog) {
        return new ErrorLogJpaEntity(
                errorLog.getId(),
                errorLog.getLevel().name(),
                errorLog.getSource(),
                errorLog.getMessage(),
                errorLog.getOccurredAt()
        );
    }

    // 엔티티 → 도메인 (DB 복원 - ErrorLog.restore() 사용)
    private ErrorLog toDomain(ErrorLogJpaEntity entity) {
        return ErrorLog.restore(
                entity.getId(),
                ErrorLevel.valueOf(entity.getLevel()),
                entity.getSource(),
                entity.getMessage(),
                entity.getOccurredAt()
        );
    }
}