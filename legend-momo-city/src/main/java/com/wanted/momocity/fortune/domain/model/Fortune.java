package com.wanted.momocity.fortune.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
// 모든 필드를 받는 private 생성자를 자동으로 생성
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Fortune {

    // 1부터 366까지의 운세 ID입니다.
    private final Long id;

    // 사용자에게 보여줄 운세 문구입니다.
    private final String content;

    // 운세의 긍정, 중립, 부정 유형입니다.
    private final FortuneTone tone;

    // 운세 데이터가 생성된 시각입니다.
    private final LocalDateTime createdAt;

    // 운세 데이터가 수정된 시각입니다.
    private final LocalDateTime updatedAt;

    // DB에서 조회한 운세 데이터를 도메인 객체로 복원합니다.
    public static Fortune reconstitute(
            Long id,
            String content,
            FortuneTone tone,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        // DB에서 조회한 값으로 변경 불가능한 운세 객체를 생성합니다.
        return new Fortune(id, content, tone, createdAt, updatedAt);
    }
}
