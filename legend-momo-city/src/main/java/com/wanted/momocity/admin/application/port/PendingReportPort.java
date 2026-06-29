package com.wanted.momocity.admin.application.port;

/* comment.
    미처리 신고 목록 요청 포트 — report BC가 어댑터로 구현
    관리자 대시보드 처리 대기 작업 섹션의 신고 타입 데이터 제공용
 */

import java.time.LocalDateTime;
import java.util.List;

public interface PendingReportPort {

    List<PendingReportItem> getPending(int limit);

    record PendingReportItem(
            Long reportId,
            Long reporterUserId,
            // DB의 reason 컬럼(영어 ENUM)을 한국어로 변환한 값 — FE에는 직접 노출 안 됨
            // 서비스 레이어에서 응답 DTO의 title 필드로 매핑됨
            String reasonKo,
            LocalDateTime requestedAt
    ) {}

}
