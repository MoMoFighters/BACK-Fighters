package com.wanted.momocity.user.domain.repository;

import com.wanted.momocity.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    // 유저 아이디로 유저 찾기
    Optional<User> findById(Long userId);

    // 닉네임 등록
    void registerNickname(Long aLong, String nickname);

    // 동일한 닉네임 있는지 확인
    boolean existsByNickname(String nickname);
}
