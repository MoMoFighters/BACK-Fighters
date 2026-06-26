package com.wanted.momocity.admin.presentation.api.response;

import com.wanted.momocity.admin.domain.notice.AdminNotice;

import java.time.LocalDateTime;

/* comment.
    공지 단건 상세 조회 결과를 클라이언트에 반환하는 응답 DTO
    목록과 달리 content 와 updatedAt 까지 포함된다.
 */

public record AdminNoticeDetailResponse(
        Long noticeId,
        String title,
        String content,
        boolean isPinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // 도메인 객체 → 응답 DTO 변환
    public static AdminNoticeDetailResponse from(AdminNotice notice) {
        return new AdminNoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

}
