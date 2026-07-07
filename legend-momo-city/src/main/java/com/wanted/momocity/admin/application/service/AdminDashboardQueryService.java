package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.port.*;
import com.wanted.momocity.admin.application.usecase.AdminDashboardQueryUseCase;
import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import com.wanted.momocity.admin.domain.access.AccessLogRepository;
import com.wanted.momocity.admin.domain.notice.AdminNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* comment.
    AdminDashboardQueryService 정리
    관리자 대시보드 통계를 각 BC Port 에서 수집해 6개 섹션으로 조합하는 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminDashboardQueryService implements AdminDashboardQueryUseCase {

    private final MemberStatsPort memberStatsPort;
    private final LectureStatsPort lectureStatsPort;
    private final ReportStatsPort reportStatsPort;
    private final SystemHealthPort systemHealthPort;
    private final PendingReportPort pendingReportPort;
    private final RecentReportPort recentReportPort;
    private final PendingTeacherPort pendingTeacherPort;
    private final UserNamePort userNamePort;
    private final AccessLogRepository accessLogRepository;
    private final AdminNoticeRepository adminNoticeRepository;

    /* comment.
        getDashboardSummary 처리 흐름 :
        1. cards        — 각 BC Port 에서 수치 4개 수집
        2. systemHealth — 인프라 상태 체크 후 변환
        3. pendingTasks — 신고 + 강사 목록 합쳐 requestedAt 기준 최신 5개 추출
        4. recentReports  — 최근 신고 3개 + UserNamePort 로 신고자 이름 조회
        5. recentNotices  — 공지 저장소에서 직접 최근 7개 조회
        6. recentAccessLogs — 비로그인 포함 최근 5개 + UserNamePort 로 이름 조회
        최적화: 3, 4, 6 에서 필요한 userId 를 먼저 모아 UserNamePort 를 단 1회 호출
     */


    @Override
    public DashboardSummary getDashboardSummary() {

        // 1. cards — 수치 4개
        Cards cards = new Cards(
                memberStatsPort.countAll(),
                reportStatsPort.countUnresolved(),
                memberStatsPort.countPending(),
                lectureStatsPort.countActive()
        );

        // 2. systemHealth — 인프라 상태
        SystemHealthPort.HealthStatus health = systemHealthPort.checkAll();
        SystemHealth systemHealth = new SystemHealth(
                health.webService(),
                health.database(),
                health.fileStorage(),
                health.mailService()
        );

        // 3, 4, 6 에서 필요한 데이터 먼저 조회
        List<PendingReportPort.PendingReportItem> pendingReports = pendingReportPort.getPending(5);
        List<PendingTeacherPort.PendingTeacherItem> pendingTeachers = pendingTeacherPort.getPending(5);
        List<RecentReportPort.RecentReportItem> recentItems = recentReportPort.getRecent(3);
        List<AccessLog> logs = accessLogRepository.findRecent(5);

        // 3개 섹션에서 필요한 userId 를 한 번에 모아 UserNamePort 단 1회 호출
        Set<Long> reporterIds = pendingReports.stream()
                .map(PendingReportPort.PendingReportItem::reporterUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> recentReporterIds = recentItems.stream()
                .map(RecentReportPort.RecentReportItem::reporterUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> logUserIds = logs.stream()
                .map(AccessLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> allUserIds = Stream.of(reporterIds, recentReporterIds, logUserIds)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Map<Long, UserNamePort.UserInfo> nameMap = userNamePort.getUserInfoByUserIds(allUserIds);

        // 3. pendingTasks — 신고 + 강사 합쳐서 최신 5개
        List<PendingTask> pendingTasks = Stream.concat(
                        pendingReports.stream().map(r -> new PendingTask(
                                "REPORT",
                                r.reasonKo(),
                                nameMap.getOrDefault(r.reporterUserId(), new UserNamePort.UserInfo("알 수 없음", null)).name(),
                                r.requestedAt()
                        )),
                        pendingTeachers.stream().map(t -> new PendingTask(
                                "TEACHER",
                                "강사 승인 요청",
                                t.name(),
                                t.requestedAt()
                        ))
                ).sorted(Comparator.comparing(PendingTask::requestedAt).reversed())
                .limit(5)
                .toList();

        // 4. recentReports — 최근 3개 + 신고자 이름 조회
        List<RecentReport> recentReports = recentItems.stream()
                .map(r -> new RecentReport(
                        r.reportId(),
                        nameMap.getOrDefault(r.reporterUserId(), new UserNamePort.UserInfo("알 수 없음", null)).name(),
                        r.reasonKo(),
                        r.isResolved(),
                        r.createdAt()
                ))
                .toList();

        // 5. recentNotices — 최근 7개
        List<RecentNotice> recentNotices = adminNoticeRepository
                .findAll(PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(n -> new RecentNotice(n.getId(), n.getTitle(), n.getCreatedAt(), n.isPinned()))
                .toList();

        // 6. recentAccessLogs — 최근 5개 (비로그인 포함)
        List<RecentAccessLog> recentAccessLogs = logs.stream()
                .map(log -> new RecentAccessLog(
                        log.getId(),
                        log.getIp(),
                        log.getUserId() != null ? nameMap.getOrDefault(log.getUserId(), new UserNamePort.UserInfo("알 수 없음", null)).name() : "비로그인",
                        log.getUserId() != null ? nameMap.getOrDefault(log.getUserId(), new UserNamePort.UserInfo("알 수 없음", null)).role() : null,
                        log.getAction() != AccessLogAction.FORBIDDEN,
                        log.getCreatedAt()
                ))
                .toList();

        return new DashboardSummary(
                cards, systemHealth, pendingTasks,
                recentReports, recentNotices, recentAccessLogs
        );
    }

}


