package com.wanted.momocity.fortune.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
// 모든 필드를 받는 private 생성자를 자동으로 생성
@AllArgsConstructor(access = AccessLevel.PRIVATE)
// 사용자가 특정 날짜에 뽑은 운세 기록을 표현
public class UserFortuneLog {

    private final Long id;

    // 운세를 뽑은 사용자의 ID
    private final Long userId;

    // 사용자에게 선택된 운세의 ID
    private final Long fortuneId;

    // 사용자가 운세를 뽑은 KST 기준 날짜
    private final LocalDate drawnDate;

    // 운세 기록이 생성된 시각
    private final LocalDateTime createdAt;

    // 오늘 처음 운세를 뽑은 사용자의 기록을 생성
    public static UserFortuneLog create(
            Long userId,        // 운세를 뽑은 사용자 ID
            Long fortuneId,     // 무작위로 선택된 운세 ID
            LocalDate drawnDate // KST 기준 운세를 뽑은 날짜
    ) {
        // 신규 기록이므로 DB ID와 생성 시각은 null로 전달
        return new UserFortuneLog(null, userId, fortuneId, drawnDate, null);
    }

    // DB에서 조회한 운세 기록을 도메인 객체로 복원합니다.
    public static UserFortuneLog reconstitute(
            Long id,                // DB에 저장된 기록 ID입니다.
            Long userId,            // DB에 저장된 사용자 ID입니다.
            Long fortuneId,         // DB에 저장된 운세 ID입니다.
            LocalDate drawnDate,    // DB에 저장된 운세 뽑기 날짜입니다.
            LocalDateTime createdAt // DB에 저장된 생성 시각입니다.
    ) {
        // DB에서 조회한 모든 값으로 운세 기록을 복원합니다.
        return new UserFortuneLog(id, userId, fortuneId, drawnDate, createdAt);
    }
}