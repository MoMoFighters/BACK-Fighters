package com.wanted.momocity.study.application.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/*
 * comment.
 *  자정을 걸치는 공부 구간을 날짜별로 쪼개는 공용 유틸
 *  - "23:50 시작 ~ 00:10 종료" 같은 구간이 있으면, 어제 몫(10초)과 오늘 몫(600초)으로 나눔
 *  - StudySessionAccumulatedEvent 발행 전, from~to 구간을 이 메서드로 쪼갠 뒤
 *    나온 개수만큼 이벤트를 발행하는 방식으로 사용 (TimerCommandService/SoloCommandService/
 *    MemberCommandService 등 accumulateElapsed()를 쓰는 모든 곳에서 공통으로 재사용)
 *  -
 *  자정을 2번 이상 걸치는 경우(48시간 이상 방치 등)는 정책상 발생하지 않음
 *  (24시간 자동 만료 스케줄러가 이를 방지하므로, 최대 1번의 자정만 걸친다고 가정해도 안전)
 *  다만 혹시 모를 경우를 대비해 while 루프로 일반화해서 처리함
 * */

public class MidnightSplitter {

    private MidnightSplitter() {
    }

    // from~to 구간을 자정 기준으로 쪼개서 (날짜, 해당 날짜 몫 초) 리스트로 반환
    public static List<DateSeconds> split(LocalDateTime from, LocalDateTime to) {
        List<DateSeconds> result = new ArrayList<>();

        LocalDateTime cursor = from;
        while (cursor.toLocalDate().isBefore(to.toLocalDate())) {
            // cursor가 속한 날짜의 자정(다음날 00:00) 직전까지가 이 날짜의 몫
            LocalDateTime endOfDay = LocalDateTime.of(cursor.toLocalDate(), LocalTime.MAX);
            long seconds = java.time.Duration.between(cursor, endOfDay).getSeconds() + 1; // MAX는 23:59:59.999... 라 1초 보정
            result.add(new DateSeconds(cursor.toLocalDate(), (int) Math.max(seconds, 0)));

            cursor = LocalDateTime.of(cursor.toLocalDate().plusDays(1), LocalTime.MIN);
        }

        // 마지막(또는 자정을 안 걸친 경우 유일한) 구간
        long lastSeconds = java.time.Duration.between(cursor, to).getSeconds();
        if (lastSeconds > 0) {
            result.add(new DateSeconds(to.toLocalDate(), (int) lastSeconds));
        }

        return result;
    }

    // 날짜 + 그 날짜 몫의 초
    public record DateSeconds(LocalDate date, int seconds) {
    }

}
