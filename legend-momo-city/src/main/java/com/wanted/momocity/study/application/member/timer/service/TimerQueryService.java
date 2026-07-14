package com.wanted.momocity.study.application.member.timer.service;

import com.wanted.momocity.study.application.common.service.StudyLapService;
import com.wanted.momocity.study.application.member.timer.usecase.TimerQueryUseCase;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.presentation.api.response.common.LapItem;
import com.wanted.momocity.study.presentation.api.response.member.timer.MemberLapListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

/*
 * comment.
 *  그룹방 내 개인 타이머 읽기 작업 UseCase 구현체
 *  - 특정 멤버의 랩 목록 조회
 *  -
 *  검증 순서 :
 *  1. 요청자가 이 방의 현재 참가자(JOINED)인지 확인 - 아니면 403
 *  2. 대상(targetUserId)이 이 방의 현재 참가자인지 확인 - 아니면 404
 *  둘 다 통과해야 랩 목록을 조회 가능 (같은 방 사람들끼리만 서로의 랩을 볼 수 있음)
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimerQueryService implements TimerQueryUseCase {

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final StudyLapService studyLapService;

    @Override
    public MemberLapListResponse getMemberLaps(Long requesterId, Long roomId, Long targetUserId) {

        boolean requesterIsMember = groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, requesterId)
                .map(GroupRoomMember::isJoined)
                .orElse(false);
        if (!requesterIsMember) {
            throw new StudyAccessDeniedException("그룹방 참가자만 조회할 수 있습니다.");
        }

        GroupRoomMember target = groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, targetUserId)
                .filter(GroupRoomMember::isJoined)
                .orElseThrow(() -> new StudyNotFoundException("그룹방 참가자가 아닙니다."));

        var laps = studyLapService.getLaps(roomId, target.getId());

        var items = IntStream.range(0, laps.size())
                .mapToObj(i -> {
                    var lap = laps.get(i);
                    return new LapItem(i + 1, lap.getStartedAt(), lap.getEndedAt(), lap.getSeconds());
                })
                .toList();

        log.info("[Study] 멤버 랩 목록 조회 완료 | roomId={}, targetUserId={}, count={}",
                roomId, targetUserId, items.size());
        return new MemberLapListResponse(targetUserId, items);
    }

}
