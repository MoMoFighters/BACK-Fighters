package com.wanted.momocity.fortune.presentation.api.response;

import com.wanted.momocity.fortune.application.result.DrawFortuneResult;
import com.wanted.momocity.fortune.domain.model.Fortune;

import java.time.LocalDate;
// 운세 응답 DTO
public record FortuneResponse(
         Long fortuneId,
         String content,
         String tone,
         LocalDate drawnDate
) {
    public static FortuneResponse from(DrawFortuneResult result) {
        return new FortuneResponse(
                result.fortuneId(),
                result.content(),
                result.tone().name(),
                result.drawnDate()
        );
    }
}
