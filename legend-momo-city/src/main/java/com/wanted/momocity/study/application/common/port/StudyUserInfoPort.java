package com.wanted.momocity.study.application.common.port;

import com.wanted.momocity.auth.domain.model.User;

import java.util.Optional;

/*
 * comment.
 *  user 컨텍스트 소유의 사용자 정보를 READ 전용으로 조회
 *  - study 컨텍스트 전용 Port (Community의 UserInfoPort와 동일 패턴)
 *  - 구현체 : infrastructure.catalog.UserInfoAdapter (Community 것과 별도로, study 전용으로 하나 더 둔다)
 * */

public interface StudyUserInfoPort {

    Optional<User> findById(Long userId);

}