package com.wanted.momocity.fortune.application.result;

import com.wanted.momocity.fortune.domain.model.Fortune;
import com.wanted.momocity.fortune.domain.model.FortuneTone;

import java.time.LocalDate;

// 운세 도메인 객체와 날짜를 서비스 결과로 반환
public record DrawFortuneResult(
        Long fortuneId,
        String content,
        FortuneTone tone,
        LocalDate drawnDate
) {
    public static DrawFortuneResult from(
            Fortune fortune,
            LocalDate drawnDate
    ){
        return new DrawFortuneResult(
                fortune.getId(),
                fortune.getContent(),
                fortune.getTone(),
                drawnDate
        );
    }
}
