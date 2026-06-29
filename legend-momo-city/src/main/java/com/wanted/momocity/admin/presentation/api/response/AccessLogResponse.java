package com.wanted.momocity.admin.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.admin.application.port.UserNamePort;
import com.wanted.momocity.admin.domain.access.AccessLog;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// FE 가 받을 JSON 최상위 구조
public record AccessLogResponse(
        List<Item> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    // Page<AccessLog> + userId→이름/역할 Map 을 받아 AccessLogResponse 로 변환
    public static AccessLogResponse from(Page<AccessLog> pageResult, Map<Long, UserNamePort.UserInfo> userInfoMap) {
        List<Item> items = pageResult.getContent().stream()
                .map(log -> Item.from(log, userInfoMap))
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
            String userName,
            String userRole
    ) {
        // userId 로 Map 에서 이름/역할 조회 — 비로그인(null) 이면 둘 다 null
        public static Item from(AccessLog log, Map<Long, UserNamePort.UserInfo> userInfoMap) {
            UserNamePort.UserInfo info = log.getUserId() != null ? userInfoMap.get(log.getUserId()) : null;
            return new Item(
                    log.getId(),
                    log.getUserId(),
                    log.getIp(),
                    log.getAction().name(),
                    log.getCreatedAt(),
                    info != null ? info.name() : null,
                    info != null ? info.role() : null
            );
        }
    }

}
