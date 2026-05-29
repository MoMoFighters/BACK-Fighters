package com.wanted.momocity.global.presentation.api.common;

import java.time.LocalDateTime;

/*
 * ApiResponse는 성공 응답의 공통 표현 형식이다.
 * presentation 계층이 외부 계약을 표준화하고, 안쪽 계층은 HTTP 응답 구조를 몰라도 되게 만든다.
 * status는 HTTP 의미를, code와 message는 비즈니스 의미를 전달한다.
 */
/*comment
*  Instant는 절대 시간점으로 글로벌 서비스에서 활용 - 우리나라랑 시간대 안 맞음
*  -> LocalDateTime으로 변경 : 얘는 말 그대로 로컬 시간 - 우리나라 시간대
* */
public record ApiResponse<T>(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), 200, code, message, data);
    }

    // 생성과 일반 성공을 분리해두면 controller가 REST 의도를 더 명확히 표현할 수 있다.
    public static <T> ApiResponse<T> created(String code, String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), 201, code, message, data);
    }

    public static ApiResponse<Void> success(String code, String message) {
        return new ApiResponse<>(LocalDateTime.now(), 200, code, message, null);
    }
}
