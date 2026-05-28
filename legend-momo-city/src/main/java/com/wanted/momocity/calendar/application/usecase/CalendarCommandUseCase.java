package com.wanted.momocity.calendar.application.usecase;

/*
 * comment.
 *  Calendar 컨텍스트의 쓰기 작업 전용 UseCase
 *  → 상태를 변경하는 작업만 담당
 *  -
 *  handle() 오버로딩으로 Command 종류별 처리
 *  → Controller 에서 CalendarCommandUseCase 1개만 주입
 *  -
 *  [담당 Command]
 *  - CreateTodoCommand  : Todo 생성
 *  - UpdateTodoCommand  : Todo 수정
 *  - DeleteTodoCommand  : Todo 삭제
 *  - CheckTodoCommand   : Todo 체크 상태 변경
 *  - CreateMemoCommand  : Memo 생성
 *  - UpdateMemoCommand  : Memo 수정
 *  - DeleteMemoCommand  : Memo 삭제
 */

import com.wanted.momocity.calendar.application.command.*;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;

public interface CalendarCommandUseCase {

    TodoResponse handle(CreateTodoCommand command);
    TodoResponse handle(UpdateTodoCommand command);
    void handle(DeleteTodoCommand command);
    TodoResponse handle(CheckTodoCommand command);

    MemoResponse handle(CreateMemoCommand command);
    MemoResponse handle(UpdateMemoCommand command);
    void handle(DeleteMemoCommand command);

}
