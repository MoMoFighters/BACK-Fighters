package com.wanted.momocity.report.infrastructure.persistence;

import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/* comment.
    ReportRepositoryAdapter 정리
    도메인 ReportRepositoryAdapter 를 JPA 로 구현하는 어댑터
    Report <-> ReportJpaEntity 변환 담당
 */
@Repository
@Transactional
public class ReportRepositoryAdapter implements ReportRepository {

    private final SpringDataReportRepository repository;

    // ReportRepository 인터페이스 구현체 / Spring 은 이 클래스를 빈으로 등록해 주입
    public ReportRepositoryAdapter(SpringDataReportRepository repository) {
        this.repository = repository;
    }

    // 도메인 객체 -> 엔티티 변환 후 저장, 저장된 엔티티를 다시 도메인으로 반환하게 된다.
    // 이때 id 값을 포함한다.
    @Override
    public Report save(Report report) {
        ReportJpaEntity entity = toEntity(report);
        ReportJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    // 최근 N 개를 조회한다. - readOnly 로 성능을 최적화 시켰다.
    // 페이지네이션 없이 limit 만 사용함
    @Override
    @Transactional(readOnly = true)
    public List<Report> findRecent(int limit) {
        // PageRequest.of(0, limit) — 0페이지에서 limit개만 가져오는 방식으로 N개 제한
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // 처리 여부(isResolved) 로 필터링한 최근 N 개 조회 로직
    @Override
    @Transactional(readOnly = true)
    public List<Report> findByIsResolved(boolean isResolved, int limit) {
        return repository.findAllByIsResolvedOrderByCreatedAtDesc(isResolved, PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    // 전체 신고 수를 반환한다. (대시보드 통계용 MS-15)
    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return repository.count();
    }

    // ID 로 신고 단건 조회 : 없으면 Optional null 값으로 반환
    @Override
    @Transactional(readOnly = true)
    public Optional<Report> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    // 도메인 → 엔티티 (신규 저장 시 id=null, JPA가 auto-increment 부여)
    private ReportJpaEntity toEntity(Report report) {
        return new ReportJpaEntity(
                report.getId(),
                report.getReporterUserId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReportedUserId(),
                report.getTargetPath(),
                report.getReason().name(),
                report.getDetail(),
                report.isResolved(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }

    // 엔티티 → 도메인 (DB에서 꺼낸 값을 Report.restore()로 복원)
    private Report toDomain(ReportJpaEntity entity) {
        return Report.restore(
                entity.getId(),
                entity.getReporterUserId(),
                ReportTargetType.valueOf(entity.getTargetType()),
                entity.getTargetId(),
                entity.getReportedUserId(),
                entity.getTargetPath(),
                ReportReason.valueOf(entity.getReason()),
                entity.getDetail(),
                entity.isResolved(),
                entity.getCreatedAt(),
                entity.getResolvedAt()
        );
    }
}