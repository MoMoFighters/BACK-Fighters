package com.wanted.momocity.calendar.domain.repository;

import com.wanted.momocity.calendar.domain.model.Calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
 * comment.
 *  domain 이 저장소가 필요하다고 선언하는 인터페이스
 *  JPA 모름
 *  실제 구현체는 infrastructure.persistence.CalendarRepositoryAdapter 가 담당
 * */

public interface CalendarRepository {

    // 저장 (신규 생성 + 성장)
    Calendar save (Calendar calendar);

    // 단건 조회
    Optional<Calendar> findById(Long id);

    // 기간별 조회
    List<Calendar> findByUserIdAndDateBetween(
            Long userId, LocalDate start, LocalDate endDate
    );

    // 삭제
    void delete(Long id);

}
