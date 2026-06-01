package com.wanted.momocity.user.infrastructure.count;

import com.wanted.momocity.admin.application.port.MemberStatsPort;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Transactional
@RequiredArgsConstructor
public class MemberStatsAdapter implements MemberStatsPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public long countActive() {
        return springDataUserRepository.countByStatus(Status.ACTIVE);
    }

    @Override
    public long countActiveBefore(LocalDate date) {
        return springDataUserRepository.countByStatusAndCreatedAtBefore(
                Status.ACTIVE,
                date.atStartOfDay()
        );
    }
    /*comment
    *  특정 날짜를 넘겨받아오면 그 날의 시간에 대한 정보는 없는데
    *  DB의 createdAt은 LocalDateTime이라 시간까지 있어서
    *  atStartOfDay()는 해당 날짜의 00:00:00으로 만들어줌 */
}
