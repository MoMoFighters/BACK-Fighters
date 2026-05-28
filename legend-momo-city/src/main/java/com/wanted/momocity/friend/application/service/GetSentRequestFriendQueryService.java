package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.usecase.GetSentRequestFriendQueryUseCase;
import com.wanted.momocity.friend.domain.repository.FriendRepository;
import com.wanted.momocity.friend.infrastructure.persistence.FriendJpaEntity;

import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GetSentRequestFriendQueryService implements GetSentRequestFriendQueryUseCase {

    private final FriendRepository friendRepository;

    //보낸 친구 요청 목록
    @Override
    public List<SentRequestView> handle(Long userId) {
        log.info("[GetSentRequestFriendQueryService] 보낸 친구 요청 목록 조회 요청 진입 - 조회 요청 유저ID: {}", userId);

        //로그인한 유저가 보낸 SENT 행
        List<FriendJpaEntity> friends = friendRepository.findSentRequestsByFromUserId(userId, "SENT");
        log.info("[GetSentRequestFriendQueryService] DB 보낸 친구 요청 데이터 로드 완료 - 찾아낸 행 수: {}개", friends.size());

        List<SentRequestView> result = new ArrayList<>();

        for (FriendJpaEntity friend : friends) {
            //SENT가 아니면 넘어가기
            if (!"SENT".equals(friend.getStatus())) {
                continue;
            }

            //상대방 유저 객체는 toUserId
            UserWithFMJpaEntity targetUser = friend.getToUserId();

            //강사는 보낸 친구 요청 목록에 뜨면 안됨
            if ("TEACHER".equals(targetUser.getRole())) {
                log.info("[GetSentRequestFriendQueryService] TEACHER 역할 유저 필터링 - 강사ID: {}", targetUser.getId());
                continue;
            }

            //결과 리스트에 담기
            result.add(new SentRequestView(
                    targetUser.getId(),
                    targetUser.getNickname(),
                    targetUser.getRole(),
                    friend.getStatus(),
                    !"ACTIVE".equals(targetUser.getStatus()),
                    targetUser.getProfileImageUrl()
            ));
        }

        log.info("[GetSentRequestFriendQueryService] 최종 보낸 친구 요청 목록 가공 완료 - 반환할 개수: {}개", result.size());
        return result;
    }
}
