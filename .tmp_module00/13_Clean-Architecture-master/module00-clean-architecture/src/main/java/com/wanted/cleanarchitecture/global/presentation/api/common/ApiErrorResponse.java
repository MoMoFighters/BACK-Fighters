package com.wanted.cleanarchitecture.global.presentation.api.common;

import java.time.Instant;

/*
 * ApiErrorResponse는 예외 상황을 외부 클라이언트에 일관된 형태로 전달하기 위한 표준 모델이다.
 * domain/application 예외가 그대로 노출되지 않고, presentation 규약에 맞게 번역된다는 점이 중요하다.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message
) {

    public static ApiErrorResponse of(int status, String code, String message) {
        return new ApiErrorResponse(Instant.now(), status, code, message);
    }
}
