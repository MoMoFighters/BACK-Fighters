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

}
