package com.wanted.momocity.study.application.solo.usecase;

import com.wanted.momocity.study.application.solo.result.SoloActionResult;

/*
 * comment.
 *  솔로 세션 쓰기 작업 전용 UseCase 인터페이스
 *  - 시작(재개 포함)/일시정지/종료
 * */

public interface SoloCommandUseCase {

    // 솔로 세션 시작 (신규 시작 + 일시정지 후 재개 통합, Result의 action 필드로 구분)
    SoloActionResult start(Long userId);

    // 솔로 세션 일시정지
    SoloActionResult pause(Long userId);

    // 솔로 세션 종료 (최종 확정 - DailyStudyRecord/MonthlyStudyRecord에 반영됨)
    SoloActionResult end(Long userId);

    // 스케줄러 전용 - 특정 세션 id를 지정해서 종료
    // 조회 시점과 처리 시점 사이에 다른 세션으로 바뀌었으면(=현재 활성 세션 id가 다르면) 아무 것도 하지 않고 넘어감
    SoloActionResult endIfMatches(Long userId, Long expectedSessionId);

}
