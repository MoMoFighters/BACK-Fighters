package com.wanted.momocity.calendar.presentation.api;

import com.wanted.momocity.calendar.application.command.*;
import com.wanted.momocity.calendar.application.usecase.*;
import com.wanted.momocity.calendar.presentation.api.common.CalendarResponseCode;
import com.wanted.momocity.calendar.presentation.api.request.*;
import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/*
 * comment.
 *  HTTP 요청을 받아서 UseCase 에 전달하고 응답 반환
 *  비지니스 로직 없음, HTTP 반환만 담당
 * */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
public class CalendarController {
    private final GetDailyCalendarUseCase getDailyCalendarUseCase;
    private final CreateTodoUseCase createTodoUseCase;
    private final UpdateTodoUseCase updateTodoUseCase;
    private final DeleteTodoUseCase deleteTodoUseCase;
    private final CheckTodoUseCase checkTodoUseCase;
    private final CreateMemoUseCase createMemoUseCase;
    private final UpdateMemoUseCase updateMemoUseCase;
    private final DeleteMemoUseCase deleteMemoUseCase;

    // 날짜별 캘린더 조회
    // GET /api/v1/calendar/daily?date=2026-05-26
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyCalendarResponse>> getDailyCalendar (
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date
    ) {

        Long userId = 1L;

        DailyCalendarResponse response = getDailyCalendarUseCase
                .getDailyCalendar(userId, date);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CalendarResponseCode.DAILY_CALENDAR_FOUND,
                        "날짜별 캘린더 데이터를 조회했습니다.",
                        response
                )
        );

    }

    // Todo 등록
    // POST /api/v1/calendar/todo
    @PostMapping("/todo")
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
            @RequestParam @Valid CreateTodoRequest request
    ) {

        Long userId = 1L;

        CreateTodoCommand command = new CreateTodoCommand(
                userId,
                request.title(),
                request.start()
        );

        TodoResponse response = createTodoUseCase.createTodo(command);

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        CalendarResponseCode.TODO_CREATED,
                        "Todo 가 등록되었습니다.",
                        response
                )
        );

    }

    // Todo 수정
    // PATCH /api/v1/calendar/todo/{calendarId}
    @PatchMapping("/todo/{calendarId}")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @PathVariable Long calendarId,
            @RequestBody @Valid UpdateTodoRequest request
    ) {

        Long userId = 1L;

        UpdateTodoCommand command = new UpdateTodoCommand(
                userId,
                calendarId,
                request.title(),
                request.start()
        );

        TodoResponse response = updateTodoUseCase.updateTodo(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CalendarResponseCode.TODO_UPDATED,
                        "Todo 가 수정되었습니다.",
                        response
                )
        );

    }

    // Todo 삭제
    // DELETE /api/v1/calendar/todo/{calendarId}
    @DeleteMapping("/todo/{calendarId}")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @PathVariable Long calendarId
    ) {
        Long userId = 1L;

        DeleteTodoCommand command = new DeleteTodoCommand(userId, calendarId);
        deleteTodoUseCase.deleteTodo(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CalendarResponseCode.TODO_DELETED,
                        "Todo 가 삭제되었습니다."
                )
        );
    }

    // Todo 체크 상태 변경
    // PATCH /api/v1/calendar/todo/{calendarId}/check
    @PatchMapping("/todo/{calendarId}/check")
    public ResponseEntity<ApiResponse<TodoResponse>> checkTodo(
            @PathVariable Long calendarId,
            @RequestBody @Valid CheckTodoRequest request
    ) {

        Long userId = 1L;

        CheckTodoCommand command = new CheckTodoCommand(
                userId,
                calendarId,
                request.isCompleted()
        );

        TodoResponse response = checkTodoUseCase.checkTodo(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CalendarResponseCode.TODO_CHECKED,
                        "Todo 체크 상태가 변경되었습니다.",
                        response
                )
        );

    }

    // Memo 등록
    // POST /api/v1/calendar/memo
    @PostMapping("/memo")
    public ResponseEntity<ApiResponse<MemoResponse>> createMemo(
            @RequestBody @Valid CreateMemoRequest request
    ) {

        Long userId = 1L;

        CreateMemoCommand command = new CreateMemoCommand(
                userId,
                request.title(),
                request.start(),
                request.end()
        );

        MemoResponse response = createMemoUseCase.createMemo(command);

        return ResponseEntity.status(201).body(
                ApiResponse.created(
                        CalendarResponseCode.MEMO_CREATED,
                        "메모가 등록되었습니다.",
                        response
                )
        );

    }

    // Memo 수정
    // PATCH /api/v1/calendar/memo/{calendarId}
    @PatchMapping("/memo/{calendarId}")
    public ResponseEntity<ApiResponse<MemoResponse>> updateMemo(
            @PathVariable Long calendarId,
            @RequestBody @Valid UpdateMemoRequest request
    ) {

        Long userId = 1L;

        UpdateMemoCommand command = new UpdateMemoCommand(
                userId,
                calendarId,
                request.title(),
                request.start(),
                request.end()
        );

        MemoResponse response = updateMemoUseCase.updateMemo(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CalendarResponseCode.MEMO_UPDATED,
                        "메모가 수정되었습니다.",
                        response
                )
        );

    }

    // Memo 삭제
    // DELETE /api/v1/calendar/memo/{calendarId}
    @DeleteMapping("/memo/{calendarId}")
    public ResponseEntity<ApiResponse<Void>> deleteMemo(
            @PathVariable Long calendarId
    ) {

        Long userId = 1L;

        DeleteMemoCommand command = new DeleteMemoCommand(userId, calendarId);
        deleteMemoUseCase.deleteMemo(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CalendarResponseCode.MEMO_DELETED,
                        "메모가 삭제되었습니다."
                )
        );

    }

}
