package com.wanted.momocity.admin.application.port;

/* comment.
    최근 신고 목록 요청 포트 - report BC 가 어댑터로 구현
    관리자 대시보드 최근 신고 섹션 데이터 제공용
 */

import java.time.LocalDateTime;
import java.util.List;

public interface RecentReportPort {

    // getRecent 는 필터 없이 최근 순서대로 가져오는 것
    List<RecentReportItem> getRecent(int limit);

    record RecentReportItem(
        Long reportId,
        Long reporterUserId,
        // DB의 reason 컬럼(영어 ENUM)을 한국어로 변환한 값 — FE에는 직접 노출 안 됨
        String reasonKo,
        boolean isResolved,
        LocalDateTime createdAt
    ) { }



}
