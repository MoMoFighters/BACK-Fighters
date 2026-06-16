package com.wanted.momocity.viewing.infrastructure.catalog;

import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.infrastructure.persistence.UserRepositoryAdapter;
import com.wanted.momocity.viewing.application.port.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
* comment.
*  [역할]
*  auth 컨텍스트의 User 를 Viewing 에서 READ 전용으로 조회
*  EnrollmentAccessPolicy 에서 userId 로 role 확인 시 사용
* */

@Component
@RequiredArgsConstructor
public class UserCatalogAdapter implements UserPort {

    private final UserRepositoryAdapter userRepositoryAdapter;

    @Override
    public Optional<User> findById(Long userId) {
        return userRepositoryAdapter.findById(userId);
    }
}
