package com.wanted.momocity.user.infrastructure.adminadapter;

import com.wanted.momocity.admin.application.port.UserNamePort;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.momocity.user.infrastructure.persistence.UserNameProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserNameAdapter implements UserNamePort {

    private final SpringDataUserRepository springDataUserRepository;

    // userId로 이름, 역할 반환
    @Override
    public Map<Long, UserInfo> getUserInfoByUserIds(Set<Long> userIds) {
        return springDataUserRepository.findNameAndRoleByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(
                        UserNameProjection::getId,
                        p -> new UserInfo(p.getName(), p.getRole().name())
                ));
    }
}
