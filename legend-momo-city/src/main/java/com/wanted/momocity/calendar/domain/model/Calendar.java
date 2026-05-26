package com.wanted.momocity.calendar.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.Getter;

import java.time.LocalDate;

/*
* comment.
*  Todo 와 Memo 의 규칙 명시
*  [calendar]
*  - Todo / Memo 생성 규칙 정의
*  - Todo 체크 상태 변경
*  - Memo 날짜 유효성 규칙 정의
*  -------------------------
*  toggleComplete() 에서 Memo 체크 차단 -> 도메인에서 직접 규칙 수호
*  end < start 검증 -> 도메인에서 (service 에서는 모름)
*  isOwedBy() 권한 체크 -> 도메인에서
* */

@Getter
public class Calendar {

    private Long id;
    private Long userId;
    private String title;
    private Category category;
    private LocalDate start;
    // nullable (Memo 전용)
    private LocalDate end;
    // Todo 전용
    private boolean isCompleted;
    // createdAt 은 JpaEntity 에서 관리

    public enum Category{
        TODO, MEMO
    }

    // Todo 신규 생성
    public static Calendar createTodo(
            Long userId, String title, LocalDate start
    ) {
        Calendar calendar = new Calendar();
        calendar.userId = userId;
        calendar.title = title;
        calendar.category = Category.TODO;
        calendar.start = start;
        calendar.end = null;
        calendar.isCompleted = false;
        return calendar;
    }

    // Memo 신규 생성
    public static Calendar createMemo(
            Long userId, String title, LocalDate start, LocalDate end
    ) {
        if (end != null && end.isBefore(start)) {
            throw new DomainRuleViolationException(
                    "메모의 종료일은 시작일보다 이전일 수 없습니다."
            );
        }
        Calendar calendar = new Calendar();
        calendar.userId = userId;
        calendar.title = title;
        calendar.category = Category.MEMO;
        calendar.start = start;
        calendar.end = end;
        calendar.isCompleted = false;
        return calendar;
    }

    // Todo 체크 상태 변경 (토글)
    public void toggleComplete() {
        if (this.category == Category.MEMO) {
            throw new DomainRuleViolationException(
                    "MEMO 는 체크 상태를 변경할 수 없습니다."
            );
        }
        this.isCompleted = !this.isCompleted;
    }

    // Todo / Memo 내용 수정
    public void update(String title, LocalDate start, LocalDate end) {
        if (end != null && end.isBefore(start)) {
            throw new DomainRuleViolationException(
                    "종료일은 시작일보다 이전일 수 없습니다."
            );
        }
        this.title = title;
        this.start = start;
        this.end = end;
    }

    // 본인 소유 여부 확인
    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    // DB 에서 조회한 데이터로 도메인 객체 복원용
    // create() 는 신규생성, reconstitute() 는 DB 복원
    public static Calendar reconstitute(
            Long id, Long userId, String title, Category category,
            LocalDate start, LocalDate end, boolean isCompleted
    ) {
        Calendar calendar = new Calendar();
        calendar.id = id;
        calendar.userId = userId;
        calendar.title = title;
        calendar.category = category;
        calendar.start = start;
        calendar.end = end;
        calendar.isCompleted = isCompleted;
        return calendar;
    }

}
