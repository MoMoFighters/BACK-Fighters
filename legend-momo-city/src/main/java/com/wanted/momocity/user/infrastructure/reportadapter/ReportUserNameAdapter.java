package com.wanted.momocity.user.infrastructure.reportadapter;

import com.wanted.momocity.report.application.port.ReportUserNamePort;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.momocity.user.infrastructure.persistence.UserNameProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportUserNameAdapter implements ReportUserNamePort {

    private final SpringDataUserRepository springDataUserRepository;

    // 신고자/피신고자 이름 조회
    @Override
    public Map<Long, String> getNamesByUserIds(Set<Long> userIds) {
        return springDataUserRepository.findNameAndRoleById(userIds)
                .stream()
                .collect(Collectors.toMap(
                        UserNameProjection::getId,
                        UserNameProjection::getName
                ));
    }
}
