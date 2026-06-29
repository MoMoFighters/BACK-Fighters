package com.wanted.momocity.admin.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    // @JsonInclude : null 인 필드를 Json 응답에서 아예 제거 하는 어노테이션
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(
            Long logId,
            Long userId,
            String ip,
            String action,
            LocalDateTime accessedAt,

            // port 받아오는 값이 있다면
            String userName,
            String userRole

    ) {
        // 도메인 객체 AccessLog 1건으로 변환한다.
        // action.name() 으로 enum -> String 변환 처리
        public static Item from(AccessLog log) {
            return new Item(
                    log.getId(),
                    log.getUserId(),
                    log.getIp(),
                    log.getAction().name(),
                    log.getCreatedAt(),
                    null, // userName - 수영님 완료 후 교체
                    null  // userRole - 수영님 완료 후 교체
            );
        }
    }

}
