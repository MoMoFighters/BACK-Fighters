package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.exception.ReportNotFoundException;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    ReportQueryService 정리
    ReportQueryUseCase 의 구현체
    ReportRepository 에 조회를 위임하고 UseCase 출력 형식(record)으로 감싸서 반환
    모든 메서드가 읽기 전용이라 클래스 레벨에 트랜젝션을 적용
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
    public ReportList getByIsResolved(boolean isResolved, int limit) {
        return new ReportList(reportRepository.findByIsResolved(isResolved, limit));
    }

    // id 값이 없으면 예외처리를 위한 메서드 재정의
    @Override
    public ReportDetail getById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
        return new ReportDetail(report);
    }
}