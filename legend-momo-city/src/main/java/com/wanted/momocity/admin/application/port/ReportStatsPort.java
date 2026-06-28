package com.wanted.momocity.admin.application.port;

public interface ReportStatsPort {

    // 미처리 신고 수 - dashboard cards.unresolvedReports
    // 대시보드 수정으로 인한 리팩토링
    long countUnresolved();

}
