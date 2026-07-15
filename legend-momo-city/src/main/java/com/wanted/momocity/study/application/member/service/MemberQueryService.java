package com.wanted.momocity.study.application.member.service;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import com.wanted.momocity.study.application.member.usecase.MemberQueryUseCase;
import com.wanted.momocity.study.domain.exception.StudyNotFoundException;
import com.wanted.momocity.study.domain.model.GroupRoom;
import com.wanted.momocity.study.domain.repository.GroupRoomMemberRepository;
import com.wanted.momocity.study.domain.repository.GroupRoomRepository;
import com.wanted.momocity.study.presentation.api.response.member.InvitationListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  그룹방 멤버 읽기 작업 UseCase 구현체
 *  - 내가 받은 초대 목록 조회
 * */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService implements MemberQueryUseCase {

    private final GroupRoomMemberRepository groupRoomMemberRepository;
    private final GroupRoomRepository groupRoomRepository;
    private final StudyUserInfoPort studyUserInfoPort;

    @Override
    public InvitationListResponse getMyInvitations(Long userId) {

        var invited = groupRoomMemberRepository.findAllByUserIdAndInvited(userId);

        var items = invited.stream()
                .map(member -> {
                    GroupRoom room = groupRoomRepository.findByIdAndActive(member.getGroupRoomId())
                            .orElseThrow(() -> new StudyNotFoundException("그룹방을 찾을 수 없습니다."));
                    User host = studyUserInfoPort.findById(room.getHostUserId())
                            .orElseThrow(() -> new StudyNotFoundException("사용자를 찾을 수 없습니다."));

                    return new InvitationListResponse.InvitationItem(
                            member.getId(),
                            member.getGroupRoomId(),
                            room.getHostUserId(),
                            host.getNickname(),
                            member.getInvitedAt()
                    );
                })
                .toList();

        log.info("[Study] 받은 초대 목록 조회 완료 | userId={}, count={}", userId, items.size());
        return new InvitationListResponse(items);
    }
}