package com.wanted.momocity.user.domain.model;

import com.wanted.momocity.report.domain.model.ReportTargetType;

import java.time.LocalDateTime;

public record ReportInfo(
        ReportTargetType targetType,
        String content,
        LocalDateTime createdAt, // 신고 받은 컨텐츠의 생성 날짜
        boolean isResolved
) {


}
