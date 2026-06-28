package com.wanted.momocity.user.domain.event;

public record ReportRedisEvent(Long userId, boolean isSave) {
    // isSave = true → saveReportTime
    // isSave = false → deleteReportTime
}
