package com.wanted.momocity.report.infrastructure.persistence;

import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportTargetType;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import org.springframework.data.domain.Page;
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

    // 컨트롤러 1-based page -> JPA 0-based 로 변환 후 Page 객체로 목록 + 총건수 반환
    @Override
    @Transactional(readOnly = true)
    public ReportRepository.ReportPage findRecent(int page, int size) {
        Page<ReportJpaEntity> result = repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, size));
        List<Report> reports = result.getContent().stream().map(this::toDomain).toList();
        return new ReportRepository.ReportPage(reports, result.getTotalElements());
    }

    // 처리 여부 기준 동일 방식
    @Override
    @Transactional(readOnly = true)
    public ReportRepository.ReportPage findByIsResolved(boolean isResolved, int page, int size) {
        Page<ReportJpaEntity> result = repository.findAllByIsResolvedOrderByCreatedAtDesc(isResolved, PageRequest.of(page - 1, size));
        List<Report> reports = result.getContent().stream().map(this::toDomain).toList();
        return new ReportRepository.ReportPage(reports, result.getTotalElements());
    }

    // 전체 신고 수를 반환한다. (대시보드 통계용 MS-15)
    // 미처리 신고 수 (is_resolved = false 인 것만 카운트)
    @Override
    @Transactional(readOnly = true)
    public long countUnresolved() {
        return repository.countByIsResolved(false);
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
                report.getTargetType(),
                report.getTargetId(),
                report.getReportedUserId(),
                report.getTargetPath(),
                report.getReason(),
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
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getReportedUserId(),
                entity.getTargetPath(),
                entity.getReason(),
                entity.getDetail(),
                entity.isResolved(),
                entity.getCreatedAt(),
                entity.getResolvedAt()
        );
    }
}