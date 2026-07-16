package com.wanted.momocity.study.infrastructure.catalog;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.study.application.common.port.StudyUserInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
 * comment.
 *  StudyUserInfoPort 구현체
 *  -> study 컨텍스트에서 user 정보(닉네임 등) 조회
 *  -> Community의 UserInfoAdapter와 동일한 패턴, LoadUserPort로 auth 도메인 직접 참조 방지
 * */

@Component
@RequiredArgsConstructor
public class StudyUserInfoAdapter implements StudyUserInfoPort {

    private final LoadUserPort loadUserPort;

    @Override
    public Optional<User> findById(Long userId) {
        return loadUserPort.findById(userId);
    }

}
