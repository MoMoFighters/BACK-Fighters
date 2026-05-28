package com.wanted.momocity.user.application.policy;

import com.wanted.momocity.user.domain.exception.NicknameDuplicateException;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPolicy {
    private final UserRepository userRepository;

    public void nicknamePolicy(String nickname) {

        if (userRepository.existsByNickname(nickname)) {
            throw new NicknameDuplicateException("이미 사용 중인 닉네임입니다.");
        }

    }
}
