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
    // 다른 곳에서 아래의 DTO 를 쓸 때를 위해서 남겨준 기존의 메서드이다.
    // size 필드 페이지 객체(page) 자체에서 그대로 꺼내쓰는 원래 방식이다.
    public static AdminNoticePageResponse from(Page<AdminNoticeListResponse> page) {
        return new AdminNoticePageResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber() + 1,  // 내부 0-based → FE 1-based 변환
                page.getSize()
        );
    }


    // MA-02 : 고정 공지 병합 시 page 객체의 size(9)가 아니라, 사용자가 요청한 원래 size(10)를 그대로 응답에 반영하기 위한 오버로드
    public static AdminNoticePageResponse of(Page<AdminNoticeListResponse> page, int requestedSize) {
        return new AdminNoticePageResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber() + 1,
                requestedSize
        );
    }

}
