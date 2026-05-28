package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.model.ReportStatus;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    ReportQueryService 정리
    1. 역할 : ReportQueryUseCase 의 구현체. Repository 에 조회를 위임하고 UseCase 출력 형식으로 감싸서 반환
    2. 위치 : 응용 계층 - 구현
    3. WHY @Transactional(readOnly = true) 클래스 레벨
       → 모든 메서드가 조회 전용이기 때문에 데이터가 변경될 위험은 없다
       → 실수로 save 호출 시 즉시 예외가 발생하게 된다.
    4. WHY ReportRepository 에 직접 의존 (Port 패턴 안 씀)
       → Report 는 신고 BC 자체 도메인이다. 외부 BC 가 아니다.
       → BC 경계 격리 불필요하다.
    5. ReportCommandService 와 의존성/트랜잭션 차이
       - 의존성 : Command 는 ReporterAccountPort + ReportRepository 둘 다, Query 는 ReportRepository 만
       - 트랜잭션 : Command 는 쓰기(@Transactional), Query 는 읽기(@Transactional(readOnly = true))
    6. 메서드 2개 처리 흐름
       a) getRecent(limit) : repository.findRecent(limit) 호출 → List<Report> 받음 → ReportList 로 감싸 반환
       b) getByStatus(status, limit) : repository.findByStatus(status, limit) 호출 → 동일하게 감싸 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService implements ReportQueryUseCase {

    private final ReportRepository reportRepository;

    @Override
    public ReportList getRecent(int limit) {
        return new ReportList(reportRepository.findRecent(limit));
    }

    @Override
    public ReportList getByStatus(ReportStatus status, int limit) {
        return new ReportList(reportRepository.findByStatus(status, limit));
    }
}