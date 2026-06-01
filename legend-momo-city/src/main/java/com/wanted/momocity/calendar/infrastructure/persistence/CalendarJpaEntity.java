package com.wanted.momocity.calendar.infrastructure.persistence;

import com.wanted.momocity.calendar.domain.model.Calendar;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * comment.
 *  calendar 테이블과 1:1 매핑되는 JPA 전용 클래스
 *  Domain Model(Calendar) 을 모르고 DB 컬럼 구조만 표현
 *  도메인 <-> JpaEntity 변환은 CalendarRepositoryAdapter 가 담당
 * */

@Getter
@Entity
@Table(name = "calendar")
@NoArgsConstructor
public class CalendarJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Calendar.Category category;

    @Column(name = "start", nullable = false)
    private LocalDate start;

    // nullable (Memo 전용)
    @Column(name = "end")
    private LocalDate end;

    // Todo 전용, 기본값 false
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    // Domain → JpaEntity 변환 (저장용)
    public static CalendarJpaEntity from(Calendar domain) {
        CalendarJpaEntity entity = new CalendarJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.title = domain.getTitle();
        entity.category = domain.getCategory();
        entity.start = domain.getStart();
        entity.end = domain.getEnd();
        entity.isCompleted = domain.isCompleted();
        return entity;
    }

    // JpaEntity → Domain 변환 (조회용)
    public Calendar toDomain() {
        return Calendar.reconstitute(
                id, userId, title, category,
                start, end, isCompleted
        );
    }

}
