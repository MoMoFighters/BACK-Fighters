package com.wanted.momocity.auth.domain.repository;

import com.wanted.momocity.auth.domain.model.User;

public interface UserRepository {

    // 해당 레포지토리는 port 로서 어떠한 의존성도 가지지 않는 레포지토리이다

    User register(User user);

    boolean existsByEmail(String email);
}
