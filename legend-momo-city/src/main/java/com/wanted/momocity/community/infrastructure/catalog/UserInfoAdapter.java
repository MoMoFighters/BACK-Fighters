package com.wanted.momocity.community.infrastructure.catalog;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.community.application.post.port.UserInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
* comment.
*  UserInfoPort 구현체
*  -> community 컨텍스트에서 user 정보 조회
*  -> viewing 의 UserCatalogAdapter 와 동일한 패턴
*  -> LoadUserPort 로 user 도메인 직접 참조 방지
* */

@Component
@RequiredArgsConstructor
public class UserInfoAdapter implements UserInfoPort {

    private final LoadUserPort loadUserPort;

    @Override
    public Optional<User> findById(Long userId) {
        return loadUserPort.findById(userId);
    }
}
