package com.wanted.momocity.study.application.room.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import com.wanted.momocity.study.application.room.usecase.RoomQueryUseCase;
import com.wanted.momocity.study.domain.exception.StudyAccessDeniedException;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.presentation.api.response.GroupRoomDetailResponse;
import com.wanted.momocity.study.presentation.api.response.GroupRoomListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  그룹방 자체 읽기 작업 UseCase 구현체
 *  - 방 상세 조회, 내가 속한 방 목록 조회
 *  -
 *  두 메서드 모두 GroupRoom(방 정보) + GroupRoomMember(멤버 목록) + StudyUserInfoPort(닉네임)를 조합해서 Response를 완성
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomQueryService implements RoomQueryUseCase {

    private final GroupRoomRepository groupRoomRepository;
    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final StudyUserInfoPort studyUserInfoPort;

    // 방 상세 조회 - 방 참가자(JOINED)만 조회 가능
    @Override
    public GroupRoomDetailResponse getRoomDetail(Long userId, Long roomId) {

        GroupRoom room = groupRoomRepository.findByIdAndActive(roomId)
                .orElseThrow(() -> new StudyNotFoundException("그룹방을 찾을 수 없습니다."));

        // 요청자가 이 방의 현재 참가자인지 확인 (참가자가 아니면 상세 내용을 볼 수 없음)
        boolean isMember = groupRoomMemberRepository.findByGroupRoomIdAndUserId(roomId, userId)
                .map(GroupRoomMember::isJoined)
                .orElse(false);
        if (!isMember) {
            throw new StudyAccessDeniedException("그룹방 참가자만 조회할 수 있습니다.");
        }

        User host = getUser(room.getHostUserId());

        var members = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(roomId).stream()
                .map(member -> {
                    User user = getUser(member.getUserId());
                    return new GroupRoomDetailResponse.MemberItem(
                            member.getUserId(),
                            user.getName(),
                            member.getStatus().name(),
                            member.getTimerStatus() == null ? null : member.getTimerStatus().name()
                    );
                })
                .toList();

        log.info("[Study] 그룹방 상세 조회 완료 | roomId={}, userId={}", roomId, userId);

        return new GroupRoomDetailResponse(
                room.getId(), room.getHostUserId(), host.getName(),
                room.getStatus().name(), room.getMaxMember(), members
        );
    }

    // 내가 속한(JOINED) 그룹방 목록 조회
    @Override
    public GroupRoomListResponse getMyRooms(Long userId) {

        var myMemberships = groupRoomMemberRepository.findAllByUserIdAndJoined(userId);

        var items = myMemberships.stream()
                .map(membership -> {
                    GroupRoom room = groupRoomRepository.findByIdAndActive(membership.getGroupRoomId())
                            .orElseThrow(() -> new StudyNotFoundException("그룹방을 찾을 수 없습니다."));
                    User host = getUser(room.getHostUserId());
                    // 방별 현재 인원 수 - 매번 다시 세는 대신 필요하면 추후 캐싱 고려 가능 (지금은 단순 조회)
                    int memberCount = groupRoomMemberRepository.findAllByGroupRoomIdAndJoined(room.getId()).size();

                    return new GroupRoomListResponse.RoomItem(
                            room.getId(), room.getHostUserId(), host.getName(),
                            memberCount, room.getStatus().name()
                    );
                })
                .toList();

        log.info("[Study] 내 그룹방 목록 조회 완료 | userId={}, count={}", userId, items.size());
        return new GroupRoomListResponse(items);
    }

    private User getUser(Long userId) {
        return studyUserInfoPort.findById(userId)
                .orElseThrow(() -> new StudyNotFoundException("사용자를 찾을 수 없습니다."));
    }
}