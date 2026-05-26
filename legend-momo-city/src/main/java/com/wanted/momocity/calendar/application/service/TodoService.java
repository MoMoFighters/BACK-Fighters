package com.wanted.momocity.calendar.application.service;

import com.wanted.momocity.calendar.application.command.CheckTodoCommand;
import com.wanted.momocity.calendar.application.command.CreateTodoCommand;
import com.wanted.momocity.calendar.application.command.DeleteTodoCommand;
import com.wanted.momocity.calendar.application.command.UpdateTodoCommand;
import com.wanted.momocity.calendar.application.usecase.CheckTodoUseCase;
import com.wanted.momocity.calendar.application.usecase.CreateTodoUseCase;
import com.wanted.momocity.calendar.application.usecase.DeleteTodoUseCase;
import com.wanted.momocity.calendar.application.usecase.UpdateTodoUseCase;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  Todo 관련 UseCase 구현체
 *  HTTP 모름, JPA 모름, 순수 비지니스 흐름만 담당
 *  -
 *  [담당 UseCase]
 *  - CreateTodoUseCase : Todo 등록
 *  - UpdateTodoUseCase : Todo 수정
 *  - DeleteTodoUseCase : Todo 삭제
 *  - CheckTodoUseCase : Todo 체크 상태 변경
 * */

@Service
@RequiredArgsConstructor
public class TodoService implements
        CreateTodoUseCase,
        UpdateTodoUseCase,
        DeleteTodoUseCase,
        CheckTodoUseCase {

    private final CalendarRepository calendarRepository;

    @Override
    @Transactional
    // CreateTodoUseCase
    public TodoResponse createTodo(CreateTodoCommand command) {

        // 도메인 메서드로 Todo 생성
        Calendar calendar = Calendar.createTodo(
                command.userId(),
                command.title(),
                command.start()
        );

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        return new TodoResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCategory(),
                saved.getStart(),
                saved.isCompleted()
        );

    }

    @Override
    @Transactional
    // UpdateTodoUseCase
    public TodoResponse updateTodo(UpdateTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new DomainRuleViolationException(
                        "Todo 를 찾을 수 없습니다."
                ));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new DomainRuleViolationException(
                    "본인의 Todo 만 수정할 수 있습니다."
            );
        }

        // 수정 (도메인 메서드)
        calendar.update(command.title(), command.start(), null);

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        return new TodoResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCategory(),
                saved.getStart(),
                saved.isCompleted()
        );

    }

    @Override
    @Transactional
    // DeleteTodoUseCase
    public void deleteTodo(DeleteTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new DomainRuleViolationException(
                        "Todo 를 찾을 수 없습니다."
                ));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new DomainRuleViolationException(
                    "본인의 Todo 만 삭제할 수 있습니다."
            );
        }

        // 삭제
        calendarRepository.delete(command.calendarId());

    }

    @Override
    @Transactional
    // CheckTodoUseCase
    public TodoResponse checkTodo(CheckTodoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new DomainRuleViolationException(
                        "Todo 를 찾을 수 없습니다."
                ));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new DomainRuleViolationException(
                    "본인의 Todo 만 변경할 수 있습니다."
            );
        }

        // 체크 상태 변경 (도메인 메서드)
        // Momo 호출 시 도메인에서 예외 발생
        calendar.toggleComplete();

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        return new TodoResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCategory(),
                saved.getStart(),
                saved.isCompleted()
        );

    }

}
