package com.wanted.momocity.study.application.member.timer.usecase;

/*
 * comment.
 *  그룹방 내 개인 타이머 읽기 작업 전용 UseCase 인터페이스
 *  - 특정 멤버의 랩 목록 조회 (카드 클릭 시 랩 이력 모달을 띄우는 용도)
 *  -
 *  본인 것뿐 아니라 다른 멤버의 랩도 조회 가능 (같은 방 참가자라는 전제 하에)
 *  검증(요청자가 방 멤버인지, 대상이 방 멤버인지)은 TimerQueryService가 담당
 * */

import com.wanted.momocity.study.presentation.api.response.member.timer.MemberLapListResponse;

public interface TimerQueryUseCase {

    // 특정 멤버의 현재(또는 가장 최근) 랩 목록 조회
    MemberLapListResponse getMemberLaps(Long requesterId, Long roomId, Long targetUserId);

}
