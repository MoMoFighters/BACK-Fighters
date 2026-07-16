package com.wanted.momocity.study.infrastructure.catalog;

import com.wanted.momocity.study.application.member.port.FriendCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * comment.
 *  FriendCatalogPort 구현체
 *  -> study(member) 컨텍스트에서 friend 도메인의 친구 관계를 조회
 *  -
 *  friend 도메인 내부 엔티티/테이블을 study가 직접 참조하지 않음
 *  friend 담당자가 공개하는 Port를 주입받아 위임만 하는 얇은 어댑터로 구성
 *  -
 *  TODO : friend 담당자가 아래와 같은 Port를 공개해주면 주입 및 연결
 *    public interface FriendQueryPort {
 *        boolean isFriend(Long userId1, Long userId2);
 *    }
 * */

@Component
@RequiredArgsConstructor
public class FriendCatalogAdapter implements FriendCatalogPort {

//    private final FriendQueryPort friendQueryPort;

    @Override
    public boolean isFriend(Long userId1, Long userId2) {
        // TODO: friendQueryPort.isFriend(userId1, userId2) 로 교체 필요
        throw new UnsupportedOperationException("friend 담당자의 Port 연동 대기 중입니다.");
    }
}
