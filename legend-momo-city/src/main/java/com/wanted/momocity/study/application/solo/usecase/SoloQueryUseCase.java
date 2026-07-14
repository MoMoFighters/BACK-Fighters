package com.wanted.momocity.study.application.solo.usecase;

import com.wanted.momocity.study.presentation.api.response.SoloCurrentResponse;
import com.wanted.momocity.study.presentation.api.response.SoloHistoryResponse;

import java.util.Optional;

/*
 * comment.
 *  솔로 세션 읽기 작업 전용 UseCase 인터페이스
 *  - 현재 진행 중인 세션 조회, 세션 이력 조회
 * */

public interface SoloQueryUseCase {

    /*
     * comment.
     *  현재 진행 중인 솔로 세션 조회 (새로고침/재접속 시 화면 복구용)
     *  진행 중인 세션이 없는 것은 에러가 아니라 정상 상태 -> null을 직접 반환하는 대신 Optional로 명시
     *  Controller가 Optional.isEmpty()면 STUDY-SOLO-CURRENT-EMPTY 코드로, 있으면 FETCHED로 분기한다.
     * */
    Optional<SoloCurrentResponse> getCurrent(Long userId);

    // 솔로 세션 이력 조회 (ENDED만, 최신순, 커서 기반 페이지네이션)
    SoloHistoryResponse getHistory(Long userId, Long cursor, int size);


}
