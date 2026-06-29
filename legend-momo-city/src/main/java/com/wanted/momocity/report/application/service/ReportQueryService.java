package com.wanted.momocity.report.application.service;

import com.wanted.momocity.report.application.port.ChapterParentPort;
import com.wanted.momocity.report.application.port.ChatContentPort;
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
    private final ChapterParentPort chapterParentPort;
    // CHAT 타입 신고 상세에서 채팅 내용 조회 — 정림님 ChatContentAdapter 완료 후 활성화
    private final ChatContentPort chatContentPort;


    @Override
    public ReportList getRecent(int page, int size) {
        // ReportPage 에서 목록+총건수 받아 totalPages 계산 후 반환
        ReportRepository.ReportPage reportPage = reportRepository.findRecent(page, size);
        List<Report> reports = reportPage.reports();

        // 1. userId 전체를 Set 으로 모음 (N+1 방지)
        Set<Long> userIds = reports.stream()
                .flatMap(r -> Stream.of(r.getReporterUserId(), r.getReportedUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. 한 번에 이름 조회
        Map<Long, String> userNames = reportUserNamePort.getNamesByUserIds(userIds);

        // 3. 페이지네이션 메타데이터 계산 후 반환
        int totalPages = (int) Math.ceil((double) reportPage.totalElements() / size);
        return new ReportList(reports, userNames, reportPage.totalElements(), totalPages, page, size);
    }

    // 동일 패턴, isResolved 필터 추가
    @Override
    public ReportList getByIsResolved(boolean isResolved, int page, int size) {
        ReportRepository.ReportPage reportPage = reportRepository.findByIsResolved(isResolved, page, size);
        List<Report> reports = reportPage.reports();

        // 1. userId 전체를 Set 으로 모음 (N+1 방지)
        Set<Long> userIds = reports.stream()
                .flatMap(r -> Stream.of(r.getReporterUserId(), r.getReportedUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. 한번에 이름 조회
        Map<Long, String> userNames = reportUserNamePort.getNamesByUserIds(userIds);

        // 3. 페이지네이션 메타데이터 계산 후 반환
        int totalPages = (int) Math.ceil((double) reportPage.totalElements() / size);
        return new ReportList(reports, userNames, reportPage.totalElements(), totalPages, page, size);
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

        // 2. targetType 기준으로 내용 조회 — CHAT 추가 (정림님 어댑터 연동)
        String targetContent = switch (report.getTargetType()) {
            case REVIEW  -> reviewContentPort.getContentById(report.getTargetId());
            case COMMENT -> commentContentPort.getContentById(report.getTargetId());
            case CHAT    -> chatContentPort.getContentById(report.getTargetId());
            default -> null;
        };

        // 3. CHAPTER 타입일 때만 lectureId 조회, 나머지는 null
        Long parentId = switch (report.getTargetType()) {
            case CHAPTER -> chapterParentPort.getLectureIdByChapterId(report.getTargetId());
            default -> null;
        };

        // 3. 이름 & 내용 포함한 ReportDetail 반환
        return new ReportDetail(
                report,
                names.get(report.getReporterUserId()),
                names.get(report.getReportedUserId()),
                targetContent,
                parentId
        );
    }
}