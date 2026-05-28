package com.wanted.momocity.calendar.infrastructure.persistence;

import com.wanted.momocity.calendar.domain.exception.CalendarNotFoundException;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CalendarRepositoryAdapter implements CalendarRepository {

    private final CalendarJpaRepository jpaRepository;

    @Override
    public Calendar save(Calendar calendar) {
        CalendarJpaEntity entity = CalendarJpaEntity.from(calendar);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Calendar> findById(Long id) {
        return jpaRepository.findById(id)
                .map(CalendarJpaEntity::toDomain);
    }

    @Override
    public List<Calendar> findByUserIdAndDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    ) {
        return jpaRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .map(CalendarJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!jpaRepository.existsById(id)){
            throw new CalendarNotFoundException(
                    "삭제할 할목을 찾을 수 없습니다."
            );
        }
        jpaRepository.deleteById(id);
    }

}
