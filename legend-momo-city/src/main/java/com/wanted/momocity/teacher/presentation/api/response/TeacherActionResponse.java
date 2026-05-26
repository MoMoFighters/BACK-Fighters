package com.wanted.momocity.teacher.presentation.api.response;

import java.time.Instant;

/*
 * MS-5: 강사 승인/반려 응답.
 *
 * 승인 시 status=ACTIVE, reason=null
 * 반려 시 status=REJECTED, reason=반려사유
 */
public record TeacherActionResponse(
        Long userId,
        String status,
        String reason,
        Instant processedAt
) {
}
