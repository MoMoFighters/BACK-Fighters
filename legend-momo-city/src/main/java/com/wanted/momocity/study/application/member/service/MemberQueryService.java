package com.wanted.momocity.study.application.member.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import com.wanted.momocity.study.application.member.usecase.MemberQueryUseCase;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.model.GroupRoomMember;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.presentation.api.response.member.InvitationListResponse;
import com.wanted.momocity.study.presentation.api.response.member.SentInvitationListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * comment.
 *  그룹방 멤버 읽기 작업 UseCase 구현체
 *  - 내가 받은 초대 목록 조회, 내가 보낸 초대 목록 조회
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService implements MemberQueryUseCase {

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final StudyUserInfoPort studyUserInfoPort;

    // 내가 받은 초대 목록 조회
    @Override
    public InvitationListResponse getMyInvitations(Long userId) {

        var invited = groupRoomMemberRepository.findAllByUserIdAndInvited(userId);

        if (invited.isEmpty()) {
            log.info("[Study] 받은 초대 목록 조회 완료 | userId={}, count=0", userId);
            return new InvitationListResponse(List.of());
        }

        // 필요한 방 id 전부 모아서 한 번에 조회 (N+1 방지)
        List<Long> roomIds = invited.stream().map(GroupRoomMember::getGroupRoomId).distinct().toList();
        Map<Long, GroupRoom> roomMap = roomIds.stream()
                .map(groupRoomRepository::findByIdAndActive)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(java.util.stream.Collectors.toMap(GroupRoom::getId, room -> room));

        // 살아있는 방들의 방장 id도 한 번에 모아서 조회
        List<Long> hostIds = roomMap.values().stream().map(GroupRoom::getHostUserId).distinct().toList();
        Map<Long, User> userMap = hostIds.stream()
                .map(studyUserInfoPort::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));

        // 방 또는 방장이 조회 안 되는 초대는 결과에서 제외(skip)하고, 정상인 것만 응답에 포함
        var items = invited.stream()
                .map(member -> {
                    GroupRoom room = roomMap.get(member.getGroupRoomId());
                    if (room == null) {
                        log.warn("[Study] 초대 목록 조회 중 방을 찾을 수 없어 건너뜀 | invitationId={}, roomId={}",
                                member.getId(), member.getGroupRoomId());
                        return null;
                    }
                    User host = userMap.get(room.getHostUserId());
                    if (host == null) {
                        log.warn("[Study] 초대 목록 조회 중 방장을 찾을 수 없어 건너뜀 | invitationId={}, hostUserId={}",
                                member.getId(), room.getHostUserId());
                        return null;
                    }
                    return new InvitationListResponse.InvitationItem(
                            member.getId(),
                            member.getGroupRoomId(),
                            room.getTitle(),
                            room.getHostUserId(),
                            host.getNickname(),
                            member.getInvitedAt()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        log.info("[Study] 받은 초대 목록 조회 완료 | userId={}, count={}", userId, items.size());
        return new InvitationListResponse(items);
    }

    // 내가 보낸 초대 목록 조회
    @Override
    public SentInvitationListResponse getSentInvitations(Long userId) {

        // 내가 보낸 초대 = 내가 방장인 방들의 INVITED 상태 멤버 전부
        var myRooms = groupRoomRepository.findAllByHostUserIdAndActive(userId);

        var items = myRooms.stream()
                .flatMap(room -> groupRoomMemberRepository.findAllByGroupRoomIdAndInvited(room.getId()).stream()
                        .map(member -> {

                            // 피초대자 유저 정보 없는 경우 초대 항목 제외
                            var inviteeOpt = studyUserInfoPort.findById(member.getUserId());
                            if (inviteeOpt.isEmpty()) {
                                log.warn("[Study] 보낸 초대 목록 조회 중 피초대자를 찾을 수 없어 건너뜀 | invitationId={}, inviteeId={}",
                                        member.getId(), member.getUserId());
                                return null;
                            }

                            User invitee = inviteeOpt.get();
                            return new SentInvitationListResponse.SentInvitationItem(
                                    member.getId(),
                                    room.getId(),
                                    room.getTitle(),
                                    member.getUserId(),
                                    invitee.getNickname(),
                                    invitee.getProfileImageUrl(),
                                    member.getInvitedAt()
                            );
                        }))
                // 피초대자 = null 인 경우 결과 제외
                .filter(java.util.Objects::nonNull)
                .toList();

        log.info("[Study] 보낸 초대 목록 조회 완료 | userId={}, count={}", userId, items.size());
        return new SentInvitationListResponse(items);
    }
}