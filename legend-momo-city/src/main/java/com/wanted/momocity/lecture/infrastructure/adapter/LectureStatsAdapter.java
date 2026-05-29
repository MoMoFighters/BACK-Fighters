package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.admin.application.port.LectureStatsPort;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataLectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}