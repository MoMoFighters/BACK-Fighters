package com.wanted.momocity.admin.presentation.api.response;

import java.time.LocalDateTime;
import java.util.List;

/* comment.
    ErrorLogResponse 정리
    1. 이 record 의 역할 : Error 로그 목록을 HTTP 응답으로 보내는 DTO
    2. 위치 : admin/presentation/api/response (표현 계층 - 출력 DTO)
    3. 왜 wrapper 패턴 (items 한 필드만) : 향후 totalCount, hasMore 등 추가 가능.
    4. 강사 영역 TeacherApplicationListResponse 와 같은 구조 이유 : TeacherApplicationListResponse 와 동일 구조
    5. 응용 Result(ErrorLogList) 와 분리하는 이유 : ErrorLogList 는 응용 출력, ErrorLogResponse 는 HTTP 출력
 */
public record ErrorLogResponse(
        List<Item> items
) {

    /* comment.
        Item 정리
        1. 이 record 의 역할 : Error 한 건의 표현 형태
        2. 왜 level 이 String 인가 (ErrorLevel enum 안 노출) : ErrorLevel.name() 으로 변환
        표현 계층은 enum 노출 안한다.
        3. 필드 5개 의미 : id (식별자), level (심각도), source (출처), message (메시지), occurredAt (발생 시각)
     */
    public record Item(
            Long id,
            String level,
            String source,
            String message,
            LocalDateTime occurredAt
    ) { }
}