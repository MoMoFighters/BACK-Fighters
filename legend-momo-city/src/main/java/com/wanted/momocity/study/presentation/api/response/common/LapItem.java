package com.wanted.momocity.study.presentation.api.response.common;

import java.time.LocalDateTime;

/*
 * comment.
 *  공부 랩(구간) 정보 공용 레코드
 *  - solo/member.timer 양쪽의 start/pause/end 응답, 그리고 랩 목록 조회 응답에서 공통 재사용
 *  - lapNumber는 DB에 저장된 값이 아니라, 조회된 랩 목록의 순번(시작 순서 기준 1부터)을 각 도메인 Service가 매겨서 채워 넣음
 *    (StudyLapService는 순번을 모르고 저장/조회만 담당)
 *  - endedAt/seconds가 null이면 아직 진행 중인 랩을 의미
 * */

public record LapItem(
        int lapNumber,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer seconds
) {
}
