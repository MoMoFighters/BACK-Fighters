package com.wanted.momocity.admin.presentation.api.response;

import com.wanted.momocity.admin.domain.access.AccessLog;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

// FE 가 받을 JSON 최상위 구조
public record AccessLogResponse(
        List<Item> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    // Controller 에서 한 줄로 변환할 수 있게 만든 메서드이다.
    // Page <AccessLog> (도메인 + 페이지 정보) 를 받아서 AccessLogResponse 로 변환
    public static AccessLogResponse from(Page<AccessLog> pageResult) {
        List<Item> items = pageResult.getContent().stream()
                .map(Item::from)
                .toList();
        return new AccessLogResponse(
                items,
                pageResult.getNumber() + 1,
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    // 로그 1건의 JSON 표현
    // 도메인 객체 AccessLog 를 FE 가 받는 필드명으로 바꾼다.
    public record Item(
            Long logId,
            Long userId,
            String ip,
            String action,
            LocalDateTime accessedAt
    ) {
        // 도메인 객체 AccessLog 1건으로 변환한다.
        // action.name() 으로 enum -> String 변환 처리
        public static Item from(AccessLog log) {
            return new Item(
                    log.getId(),
                    log.getUserId(),
                    log.getIp(),
                    log.getAction().name(),
                    log.getCreatedAt()
            );
        }
    }

}
