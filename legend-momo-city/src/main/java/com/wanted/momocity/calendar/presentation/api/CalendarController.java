package com.wanted.momocity.calendar.presentation.api;

import com.wanted.momocity.calendar.application.command.*;
import com.wanted.momocity.calendar.application.usecase.*;
import com.wanted.momocity.calendar.presentation.api.common.CalendarResponseCode;
import com.wanted.momocity.calendar.presentation.api.request.*;
import com.wanted.momocity.calendar.presentation.api.response.DailyCalendarResponse;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Calendar", description = "Calendar 도메인 - Todo/Memo CRUD 및 날짜별 조회 API")
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
    @Operation(summary = "날짜별 캘린더 조회",
            description = "날짜를 기준으로 Todo 와 Memo 를 분리하여 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "날짜 파라미터 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping("/daily")
    // HTTP 응답 객체<공통 응답 형식<실제 데이터 타입>>
    // (status code, header, body)<(success, statusCode, message, data, error)<(date, todos[], errors[])>>
    public ResponseEntity<ApiResponse<DailyCalendarResponse>> getDailyCalendar (
            // ISO.DATE 형식 : YYYY-MM-DD, 2026-05-26 (String) -> LocalDate.of(2026, 5, 26) 자동 변환
            // @RequestParam : URL 쿼리 파라미터에서 가져옴
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
    @Operation(summary = "Todo 등록",
            description = "새로운 Todo 를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수값 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @PostMapping("/todo")
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
            // @RequestBody : HTTP Body 에서 JSON 으로 가져옴
            @RequestBody @Valid CreateTodoRequest request
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
    @Operation(summary = "Todo 수정",
            description = "기존 Todo 를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수값 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo 없음")
    })
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
    @Operation(summary = "Todo 삭제",
            description = "기존 Todo 를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo 없음")
    })
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
    @Operation(summary = "Todo 체크 상태 변경",
            description = "Todo 의 완료 상태를 변경합니다. Memo 에는 호출 불가.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Memo 체크 불가"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Todo 없음")
    })
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
    @Operation(summary = "Memo 등록",
            description = "새로운 Memo 를 등록합니다. end 는 nullable.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수값 누락 또는 end < start"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
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
    @Operation(summary = "Memo 수정",
            description = "기존 Memo 를 수정합니다. end 는 nullable.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수값 누락 또는 end < start"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Memo 없음")
    })
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
    @Operation(summary = "Memo 삭제",
            description = "기존 Memo 를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Memo 없음")
    })
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
