package com.wanted.momocity.user.infrastructure.adminadapter;

import com.wanted.momocity.admin.application.port.MemberStatsPort;
import com.wanted.momocity.admin.application.port.MonthlyCount;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.infrastructure.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Transactional
@RequiredArgsConstructor
public class MemberStatsAdapter implements MemberStatsPort {

    private final SpringDataUserRepository springDataUserRepository;

    // 전체 회원 수 (탈퇴 제외)
    @Override
    public long countAll() {
        return springDataUserRepository.countByStatusNot(Status.DELETED);
    }

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

    // 해당 연도의 월별 회원 수 조회
    public List<MonthlyCount> countMemberByMonth(int year){
        Map<Integer, Long> monthMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, 0L);

        springDataUserRepository.countByMonth(year)
                .forEach(mc -> monthMap.put(mc.month(), mc.count()));

        return monthMap.entrySet().stream()
                .map(e -> new MonthlyCount(e.getKey(), e.getValue()))
                .toList();
    }

    // 승인 대기 중인 강사 수
    @Override
    public long countPending() {
        return springDataUserRepository.countByRoleAndStatus(Role.TEACHER, Status.PENDING);
    }
    /*comment
    *  특정 날짜를 넘겨받아오면 그 날의 시간에 대한 정보는 없는데
    *  DB의 createdAt은 LocalDateTime이라 시간까지 있어서
    *  atStartOfDay()는 해당 날짜의 00:00:00으로 만들어줌 */
}
