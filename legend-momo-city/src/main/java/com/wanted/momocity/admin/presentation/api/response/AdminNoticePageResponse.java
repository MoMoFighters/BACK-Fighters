package com.wanted.momocity.admin.presentation.api.response;

import org.springframework.data.domain.Page;

import java.util.List;

// Spring Page 의 기본 직렬화 필드명(content)이 FE 스펙(items)과 달라 별도 래퍼로 변환
public record AdminNoticePageResponse(
        List<AdminNoticeListResponse> items,
        //FE 컨벤션 통일 — 접근로그(page/size) 기준으로 필드명 맞춤
        int totalPages,
        long totalElements,
        int page,
        int size
) {
    public static AdminNoticePageResponse from(Page<AdminNoticeListResponse> page) {
        return new AdminNoticePageResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber() + 1,  // 내부 0-based → FE 1-based 변환
                page.getSize()
        );
    }
}
