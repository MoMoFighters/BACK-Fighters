package com.wanted.momocity.admin.presentation.api.response;

import com.wanted.momocity.admin.domain.notice.AdminNotice;

import java.time.LocalDateTime;

/* comment.
    공지 목록 조회 결과를 클라이언트에 반환하는 응답 DTO.
    도메인 객체를 HTTP 응답 JSON 변환
 */

public record AdminNoticeListResponse(
        Long noticeId,
        String title,
        boolean isPinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    // 도메인 객체 -> 응답 DTO 반환
    public static AdminNoticeListResponse from(AdminNotice notice) {
        return new AdminNoticeListResponse(
                notice.getId(),
                notice.getTitle(),
                notice.isPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

}
