package com.wanted.momocity.community.application.port;

import com.wanted.momocity.auth.domain.model.User;

import java.util.Optional;

/*
* comment.
*  user 켄텍스트 소유의 사용자 정보를 READ 전용으로 조회
*  - community 컨텍스트 전용 Port
*  - community 가 user 도메인을 직접 참조하지 않고 이 포트를 통해서만 접근
*  - 구현체 : UserInfoAdapter
* */

public interface UserInfoPort {

    Optional<User> findById(Long userId);

}
