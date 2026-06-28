package com.wanted.momocity.admin.application.port;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
    미승인 강사 목록 요청 포트 - User BC 수영님이 어댑터로 구현
    관리자 대시보드 처리 대기 작업 섹션의 강사 승인 타입 데이터 제공용
 */

public interface PendingTeacherPort {

    List<PendingTeacherItem> getPending(int limit);

    record PendingTeacherItem(
            Long userId,
            String name,
            LocalDateTime requestedAt
    ) {}

}
