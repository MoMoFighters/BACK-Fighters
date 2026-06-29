package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.admin.application.port.LectureStatsPort;
import com.wanted.momocity.admin.application.port.MonthlyCount;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataLectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 관리자 대시보드에서 필요한 강의 통계 값을 제공하는 Adapter
@Component
@RequiredArgsConstructor
public class LectureStatsAdapter implements LectureStatsPort {

    private final SpringDataLectureRepository repository;

    /* comment
     * 현재 활성화된 강의 수를 조회한다.
     * ACTIVE 상태의 강의만 사용자에게 공개되는 강의로 본다.
     */
    @Override
    public long countActive() {
        return repository.countByStatus(LectureStatus.ACTIVE);
    }

    /*
     * 특정 날짜 이전에 생성된 활성화 강의 수를 조회한다.
     * 관리자 대시보드에서 전월 대비 증감률 계산에 사용한다.
     */
    @Override
    public long countActiveBefore(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();

        return repository.countByStatusAndCreatedAtBefore(
                LectureStatus.ACTIVE,
                dateTime
        );
    }

    /*
     * 특정 연도에 등록된 강의 수를 월별로 조회한다.
     * 그래프 표시를 위해 데이터가 없는 달도 0건으로 채워 1월부터 12월까지 반환한다.
     */
    @Override
    public List<MonthlyCount> countLectureByMonth(int year) {
        Map<Integer, Long> monthlyCountMap = repository.countLectureByMonth(year).stream()
                .collect(Collectors.toMap(
                        SpringDataLectureRepository.MonthlyLectureCount::getMonth,
                        SpringDataLectureRepository.MonthlyLectureCount::getLectureCount
                ));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> new MonthlyCount(
                        month,
                        monthlyCountMap.getOrDefault(month, 0L)
                ))
                .toList();
    }
}
