package com.wanted.momocity.report.infrastructure.persistence;

import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportStatus;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/* comment.
    ReportRepositoryAdapter 정리
    1. 역할 : 도메인 ReportRepository 인터페이스 JPA 로 구현하는 어댑터 역할
    2. 위치 : 인프라 계층
    3. 핵심 책임
       - 도메인 Report 모델 <-> ReportJpaEntity 변환
       - SpringDataRepository 호출을 감싸 도메인이 인프라를 모르게 격리해버린다.
    4. WHY 의존 방향 (클린 아키텍처)
       → SpringDataRepository 주입하고 같은 계층을 호출한다
       → Report 사용을 하게 된다. 인프라는 도메인을 알아도 괜찮다.
       → 하지만 도메인은 이 클래스의 존재를 몰라야한다. (오직 인터페이스만 봐야함)
    5. WHY @Transactional 클래스 레벨 + 메서드 readOnly 오버라이드
       → 클래스 레벨 : 쓰기 트랜젝션 기본값 save 같이 데이터 변경처럼 민감한 작업의 안정성 보장
       → 조회 메서드만 오버라이드할 경우 성능 최적화가 가능하기 때문이다.
 */
@Repository
@Transactional
public class ReportRepositoryAdapter implements ReportRepository {

    private final SpringDataReportRepository repository;

    public ReportRepositoryAdapter(SpringDataReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Report save(Report report) {
        ReportJpaEntity entity = toEntity(report);
        ReportJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> findRecent(int limit) {
        return repository.findAllByOrderByReportedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> findByStatus(ReportStatus status, int limit) {
        return repository.findAllByStatusOrderByReportedAtDesc(status.name(), PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return repository.count();
    }

    // === 변환 메서드 (도메인 ↔ 엔티티) ===

    // 도메인 → 엔티티 (신규 저장 시 id=null, JPA 가 auto-increment 부여)
    private ReportJpaEntity toEntity(Report report) {
        return new ReportJpaEntity(
                report.getId(),
                report.getReporterUserId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReason().name(),
                report.getDetail(),
                report.getStatus().name(),
                report.getReportedAt(),
                report.getHandledAt(),
                report.getHandlerAdminId()
        );
    }

    // 엔티티 → 도메인 (DB 복원 - Report.restore() 사용)
    private Report toDomain(ReportJpaEntity entity) {
        return Report.restore(
                entity.getId(),
                entity.getReporterUserId(),
                ReportTargetType.valueOf(entity.getTargetType()),
                entity.getTargetId(),
                ReportReason.valueOf(entity.getReason()),
                entity.getDetail(),
                ReportStatus.valueOf(entity.getStatus()),
                entity.getReportedAt(),
                entity.getHandledAt(),
                entity.getHandlerAdminId()
        );
    }
}