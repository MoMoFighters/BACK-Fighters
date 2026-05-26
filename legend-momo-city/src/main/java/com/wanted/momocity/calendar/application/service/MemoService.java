package com.wanted.momocity.calendar.application.service;

import com.wanted.momocity.calendar.application.command.CreateMemoCommand;
import com.wanted.momocity.calendar.application.command.DeleteMemoCommand;
import com.wanted.momocity.calendar.application.command.UpdateMemoCommand;
import com.wanted.momocity.calendar.application.usecase.CreateMemoUseCase;
import com.wanted.momocity.calendar.application.usecase.DeleteMemoUseCase;
import com.wanted.momocity.calendar.application.usecase.UpdateMemoUseCase;
import com.wanted.momocity.calendar.domain.model.Calendar;
import com.wanted.momocity.calendar.domain.repository.CalendarRepository;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * comment.
 *  Memo 관련 UseCase 구현체
 *  Http 모름, JPA 모름, 순수 비지니스 흐름만 담당
 *  -
 *  [담당 UseCase]
 *  - CreateMemoUseCase : Memo 등록
 *  - UpdateMemoUseCase : Memo 수정
 *  - DeleteMemoUseCase : Memo 삭제
 * */

@Service
@RequiredArgsConstructor
public class MemoService implements
        CreateMemoUseCase,
        UpdateMemoUseCase,
        DeleteMemoUseCase {

    private final CalendarRepository calendarRepository;

    @Override
    @Transactional
    // CreateMemoUseCase
    public MemoResponse createMemo(CreateMemoCommand command) {

        // 도메인 메서드로 Memo 생성
        // end < start 검증은 도메인에서 처리
        Calendar calendar = Calendar.createMemo(
                command.userId(),
                command.title(),
                command.start(),
                command.end()
        );

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        return new MemoResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCategory(),
                saved.getStart(),
                saved.getEnd()
        );
    }

    @Override
    @Transactional
    // UpdateMemoUseCase
    public MemoResponse updateMemo(UpdateMemoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new DomainRuleViolationException(
                        "메모를 찾을 수 없습니다."
                ));

        // 본인 고유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new DomainRuleViolationException(
                    "본인의 메모만 수정할 수 있습니다."
            );
        }

        // 수정 (도메인 메서드)
        calendar.update(command.title(), command.start(), command.end());

        // 저장
        Calendar saved = calendarRepository.save(calendar);

        return new MemoResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCategory(),
                saved.getStart(),
                saved.getEnd()
        );

    }

    @Override
    @Transactional
    // DeleteMemoUseCase
    public void deleteMemo(DeleteMemoCommand command) {

        // 조회
        Calendar calendar = calendarRepository.findById(command.calendarId())
                .orElseThrow(() -> new DomainRuleViolationException(
                        "메모를 찾을 수 없습니다."
                ));

        // 본인 소유 여부 확인 (도메인 메서드)
        if (!calendar.isOwnedBy(command.userId())) {
            throw new DomainRuleViolationException(
                    "본인의 메모만 삭제할 수 있습니다."
            );
        }

        // 삭제
        calendarRepository.delete(command.calendarId());
    }

}
