package com.wanted.momocity.friend.application.service;

import com.wanted.momocity.friend.application.usecase.GetReceivedRequestFriendQueryUseCase;
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
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetReceivedRequestFriendQueryService implements GetReceivedRequestFriendQueryUseCase {

    private final FriendRepository friendRepository;

    //받은 친구 요청 목록
    @Override
    public List<ReceivedRequestView> handle(Long userId) {
        log.info("[GetReceivedRequestFriendQueryService] 받은 친구 요청 목록 조회 시작 - 수신자(로그인 유저): {}", userId);

        //toUserId가 로그인 유저이면서 SENT인 데이터만 가져옴
        List<FriendJpaEntity> requests = friendRepository.findReceivedRequestsByToUserId(userId, "SENT");
        log.info("[GetReceivedRequestFriendQueryService] DB 받은 친구 요청 데이터 로드 완료 - 찾아낸 행 수: {}", requests.size());

        //결과 담을 곳
        List<ReceivedRequestView> result = new ArrayList<>();

        //하나씩 add
        for (FriendJpaEntity request : requests) {
            //SENT가 아니면 넘어감
            if (!"SENT".equals(request.getStatus())) {
                continue;
            }

            //요청 보낸 사람 추출
            UserWithFMJpaEntity fromUser = request.getFromUserId();

            result.add(new ReceivedRequestView(
                    fromUser.getId(),
                    fromUser.getNickname(),
                    fromUser.getRole(),
                    request.getStatus(),
                    !"ACTIVE".equals(fromUser.getStatus()), //활성 유저 아니면 true
                    fromUser.getProfileImageUrl()
            ));
        }

        log.info("[GetReceivedRequestFriendQueryService] 최종 받은 친구 요청 목록 가공 완료 - 반환할 개수: {}개", result.size());

        //요청 보낸 사람 정보 담기
        return result;
    }
}
