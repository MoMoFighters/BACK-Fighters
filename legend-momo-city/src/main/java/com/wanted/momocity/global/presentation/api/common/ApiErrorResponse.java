package com.wanted.momocity.global.presentation.api.common;

import java.time.Instant;
import java.time.LocalDateTime;

/*
 * ApiErrorResponse는 예외 상황을 외부 클라이언트에 일관된 형태로 전달하기 위한 표준 모델이다.
 * domain / application 예외가 그대로 노출되지 않고, presentation 규약에 맞게 번역된다.
 */
/*comment
 *  Instant는 절대 시간점으로 글로벌 서비스에서 활용 - 우리나라랑 시간대 안 맞음
 *  -> LocalDateTime으로 변경 : 얘는 말 그대로 로컬 시간 - 우리나라 시간대
 * */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message
) {

    public static ApiErrorResponse of(int status, String code, String message) {
        return new ApiErrorResponse(LocalDateTime.now(), status, code, message);
    }
}
