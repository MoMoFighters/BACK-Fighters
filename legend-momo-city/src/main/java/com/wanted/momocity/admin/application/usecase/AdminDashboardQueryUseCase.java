package com.wanted.momocity.admin.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
    AdminDashboardQueryUseCase 정리
    관리자 대시보드 조회 유스케이스 계약.
    getDashboardSummary() 한 번 호출로 6개 섹션 데이터를
    한꺼번에 반환한다.
 */
public interface AdminDashboardQueryUseCase {

    DashboardSummary getDashboardSummary();

    // 대시보드 요약
    record DashboardSummary(
            Cards cards,
            SystemHealth systemHealth,
            List<PendingTask> pendingTasks,
            List<RecentReport> recentReports,
            List<RecentNotice> recentNotices,
            List<RecentAccessLog> recentAccessLogs
    ) {}

    // 대시보드 메인에 들어갈 내용
    record Cards(
            long totalUsers,
            long unresolvedReports,
            long pendingTeachers,
            long activeLectures
    ) {}

    // 대시보드에서 서버 상태를 확인하는 상태창
    record SystemHealth(
            String webService,
            String database,
            String fileStorage,
            String mailService
    ) {}

    // 대기중인 내역 목록
    record PendingTask(
            String type,
            String title,
            String requester,
            LocalDateTime requestedAt
    ) {}

    // 최근 신고 접수 내역
    record RecentReport(
            Long reportId,
            String reporterName,
            String reason,
            boolean isResolved,
            LocalDateTime createdAt
    ) {}

    // 최근 공지사항 목록
    record RecentNotice(
            Long noticeId,
            String title,
            LocalDateTime createdAt
    ) {}

    // 최근 접속한 사람들 로그 — role 추가 (FE MS-4 연동 요청)
    record RecentAccessLog(
            Long logId,
            String ip,
            String userName,
            String role,
            boolean isSuccess,
            LocalDateTime accessedAt
    ) {}
}