package com.wanted.momocity.admin.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
    DashboardSummaryResponse 정리
    관리자 대시보드 6개 섹션을 FE 에게 내려주는 HTTP 응답 body.
    DashboardSummary(UseCase 출력)를 그대로 표현 게층 DTO 로 변환하는 구조
 */
public record DashboardSummaryResponse(
        Cards cards,
        SystemHealth systemHealth,
        List<PendingTask> pendingTasks,
        List<RecentReport> recentReports,
        List<RecentNotice> recentNotices,
        List<RecentAccessLog> recentAccessLogs
) {
    record Cards(long totalUsers, long unresolvedReports, long pendingTeachers, long activeLectures) {}
    record SystemHealth(String webService, String database, String fileStorage, String mailService) {}
    record PendingTask(String type, String title, String requester, LocalDateTime requestedAt) {}
    record RecentReport(Long reportId, String reporterName, String reason, boolean isResolved, LocalDateTime createdAt) {}
    record RecentNotice(Long noticeId, String title, LocalDateTime createdAt) {}
    // role 추가 — FE MS-4 연동 요청
    record RecentAccessLog(Long logId, String ip, String userName, String role, boolean isSuccess, LocalDateTime accessedAt) {}
}