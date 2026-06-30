package com.wanted.momocity.community.infrastructure.adapter;

import com.wanted.momocity.admin.application.port.MonthlyCount;
import com.wanted.momocity.admin.application.port.PostStatsPort;
import com.wanted.momocity.community.infrastructure.persistence.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 대시보드 월별 운영 추이 — 게시글 수 집계 어댑터
@Component
@RequiredArgsConstructor
public class PostStatsAdapter implements PostStatsPort {

    private final PostJpaRepository postJpaRepository;

    // 해당 연도 월별 게시글 수 반환 (1월~12월), 데이터 없는 달은 0으로 채움
    @Override
    public List<MonthlyCount> countPostByMonth(int year) {
        List<Object[]> rows = postJpaRepository.countPostByMonth(year);

        Map<Integer, Long> monthMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) monthMap.put(i, 0L);

        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            monthMap.put(month, count);
        }

        return IntStream.rangeClosed(1, 12)
                .mapToObj(m -> new MonthlyCount(m, monthMap.get(m)))
                .collect(Collectors.toList());
    }

    // 특정 날짜 이전까지 등록된 게시글 총 개수
    // → 연도 간 누적 기준점 계산용
    @Override
    public long countPostBefore(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return postJpaRepository.countPostBefore(dateTime);
    }


}
