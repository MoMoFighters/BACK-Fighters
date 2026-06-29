package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.port.CommentContentPort;
import com.wanted.momocity.report.application.port.ReportUserNamePort;
import com.wanted.momocity.report.application.port.ReviewContentPort;
import com.wanted.momocity.report.application.usecase.ReportQueryUseCase;
import com.wanted.momocity.report.domain.exception.ReportNotFoundException;
import com.wanted.momocity.report.domain.model.Report;
import com.wanted.momocity.report.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final ReportUserNamePort reportUserNamePort;
    private final ReviewContentPort reviewContentPort;
    private final CommentContentPort commentContentPort;


    @Override
    public ReportList getRecent(int limit) {
        List<Report> reports = reportRepository.findRecent(limit);

        // 1. userId 전체를 Set 으로 모음 (N+1 방지)
        Set<Long> userIds = reports.stream()
                .flatMap(r -> Stream.of(r.getReporterUserId(), r.getReportedUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. 한 번에 이름 조회
        Map<Long, String> userNames = reportUserNamePort.getNamesByUserIds(userIds);

        // 3. targetType 기준으로 내용 조회
        Map<Long, String> targetContents = new HashMap<>();
        for (Report r : reports) {
            String content = switch (r.getTargetType()) {
                case REVIEW -> reviewContentPort.getContentById(r.getTargetId());
                case COMMENT -> commentContentPort.getContentById(r.getTargetId());
                default -> null;
            };
            targetContents.put(r.getTargetId(), content);
        }

        return new ReportList(reports, userNames, targetContents);
    }

    @Override
    public ReportList getByIsResolved(boolean isResolved, int limit) {
        List<Report> reports = reportRepository.findByIsResolved(isResolved, limit);

        // 1. userId 전체를 Set 으로 모음 (N+1 방지)
        Set<Long> userIds = reports.stream()
                .flatMap(r -> Stream.of(r.getReporterUserId(), r.getReportedUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. 한번에 이름 조회
        Map<Long, String> userNames = reportUserNamePort.getNamesByUserIds(userIds);

        // 3. targetType 기준으로 내용 조회
        Map<Long, String> targetContents = new HashMap<>();
        for (Report r : reports) {
            String content = switch (r.getTargetType()) {
                case REVIEW -> reviewContentPort.getContentById(r.getTargetId());
                case COMMENT -> commentContentPort.getContentById(r.getTargetId());
                default -> null;
            };
            targetContents.put(r.getTargetId(), content);
        }

        return new ReportList(reports, userNames, targetContents);
    }

    // id 값이 없으면 예외처리를 위한 메서드 재정의
    @Override
    public ReportDetail getById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));

        // 1. 신고자 & 피신고자 이름 한 번에 조회
        Map<Long, String> names = reportUserNamePort.getNamesByUserIds(
                Set.of(report.getReporterUserId(), report.getReportedUserId())
        );

        // 2. targetType 기준으로 내용 조회
        String targetContent = switch (report.getTargetType()) {
            case REVIEW -> reviewContentPort.getContentById(report.getTargetId());
            case COMMENT -> commentContentPort.getContentById(report.getTargetId());
            default -> null;
        };

        // 3. 이름 & 내용 포함한 ReportDetail 반환
        return new ReportDetail(
                report,
                names.get(report.getReporterUserId()),
                names.get(report.getReportedUserId()),
                targetContent
        );
    }
}