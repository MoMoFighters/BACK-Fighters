package com.wanted.momocity.viewing.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.usecase.*;
import com.wanted.momocity.viewing.presentation.api.common.ViewingResponseCode;
import com.wanted.momocity.viewing.presentation.api.request.SaveExitRequest;
import com.wanted.momocity.viewing.presentation.api.request.SaveProgressRequest;
import com.wanted.momocity.viewing.presentation.api.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
* comment.
*  HTTP 요청을 받아서 UseCase 에 전달하고 응답 반환
*  비지니스 로직 없음, HTTP 반환만 담당
* */

@Tag(name = "Viewing", description = "Viewing 도메인 - 강의 시청 및 진척도 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ViewingController {

    private final ViewingCommandUseCase viewingCommandUseCase;
    private final ViewingQueryUseCase viewingQueryUseCase;

    // S3 Presigned URL 발급
    // GET /api/v1/lectures/{lectureId}/chapters/{chapterId}/stream
    @Operation(summary = "S3 Presigned URL 발급",
            description = "챕터 클릭 시 해당 챕터의 영상 URL 발급. 유효시간 1시간.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URL 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "재생 불가 영상"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 권한 없음")
    })
    @GetMapping("/lectures/{lectureId}/chapters/{chapterId}/stream")
    public ResponseEntity<ApiResponse<StreamingUrlResponse>> getStreamingUrl (
            @Parameter(description = "강의 ID", required = true) @PathVariable("lectureId") Long lectureId,
            @Parameter(description = "챕터 ID", required = true) @PathVariable("chapterId") Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.STREAMING_URL_ISSUED,
                "영상 스트리밍 URL 이 발급되었습니다.",
                viewingQueryUseCase.getStreamingUrl(userId, lectureId, chapterId)
        ));

    }

    // 강의 메타데이터 조회
    // GET /api/v1/lectures/{lectureId}/meta
    @Operation(summary = "강의 메타데이터 조회",
            description = "플레이어 UI 상단에 강의 제목, 강사명, 현재 차시 표시용.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 권한 없음")
    })
    @GetMapping("/lectures/{lectureId}/meta")
    public ResponseEntity<ApiResponse<LectureMetaResponse>> getLectureMeta(
            @Parameter(description = "강의 ID", required = true) @PathVariable("lectureId") Long lectureId,
            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.LECTURE_META_FOUND,
                "강의 메타데이터 조회에 성공했습니다.",
                viewingQueryUseCase.getLectureMeta(userId, lectureId)
        ));

    }

    // 강의 영상 재생 진척도 저장
    // PATCH /api/v1/lectures/{lectureId}/chapters/{chapterId}/progress
    @Operation(summary = "강의 영상 진척도 저장",
            description = "프론트에서 5~10초 주기로 현재 재생 위치 전송. 90% 이상 시청 시 챕터 완료 처리.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수값 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 권한 없음")
    })
    @PatchMapping("/lectures/{lectureId}/chapters/{chapterId}/progress")
    public ResponseEntity<ApiResponse<SaveProgressResponse>> saveProgress(
            @Parameter(description = "강의 ID", required = true) @PathVariable("lectureId") Long lectureId,
            @Parameter(description = "챕터 ID", required = true) @PathVariable("chapterId") Long chapterId,
            @RequestBody @Valid SaveProgressRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.PROGRESS_SAVED,
                "진척도가 업데이트되었습니다.",
                viewingCommandUseCase.handle(new SaveProgressCommand(
                        userId, lectureId, chapterId, request.playbackSeconds(), null
                ))
        ));

    }

    // 강의실 나갈 때 진척고 저장
    // PATCH /api/v1/lectures/{lectureId}/chapters/{chapterId}/exit
    @Operation(summary = "강의실 나가기",
            description = "나갈 때 마지막 재생 위치 저장. 팝업 예 버튼 클릭 시 호출.")
    @PatchMapping("/lectures/{lectureId}/chapters/{chapterId}/exit")
    public ResponseEntity<ApiResponse<SaveProgressResponse>> saveExit(
            @PathVariable("lectureId") Long lectureId,
            @PathVariable("chapterId") Long chapterId,
            @RequestBody @Valid SaveExitRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.PROGRESS_SAVED,
                "마지막 재생 위치가 저장되었습니다.",
                viewingCommandUseCase.handle(new SaveProgressCommand(
                        userId, lectureId, chapterId,
                        request.playbackSeconds(),
                        request.lastPositionSec()  // ← lastPositionSec 전달
                ))
        ));
    }

    // 챕터 이어보기
    // GET /api/v1/lectures/{lectureId}/chapters/{chapterId}/resume
    @Operation(summary = "챕터 이어보기",
            description = "마지막 재생 위치 반환. 시청 기록 없으면 lastPositionSec: 0 반환.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 권한 없음")
    })
    @GetMapping("/lectures/{lectureId}/chapters/{chapterId}/resume")
    public ResponseEntity<ApiResponse<ChapterResumeResponse>> getChapterResume(
            @Parameter(description = "강의 ID", required = true) @PathVariable("lectureId") Long lectureId,
            @Parameter(description = "챕터 ID", required = true) @PathVariable("chapterId") Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.CHAPTER_RESUME_FOUND,
                "챕터 이어보기 정보를 조회했습니다.",
                viewingQueryUseCase.getChapterResume(userId, lectureId, chapterId)
        ));

    }

    // 전체 진척도 조회
    // GET /api/v1/lectures/{lectureId}/progress
    @Operation(summary = "전체 진척도 조회",
            description = "완료 챕터 durationSec + 미완료 챕터 watchedSeconds 합산으로 계산.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 권한 없음")
    })
    @GetMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<ApiResponse<TotalProgressResponse>> getTotalProgress(
            @Parameter(description = "강의 ID", required = true) @PathVariable("lectureId") Long lectureId,
            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.TOTAL_PROGRESS_FOUND,
                "전체 진척도를 조회했습니다.",
                viewingQueryUseCase.getTotalProgress(userId, lectureId)
        ));
    }

    // 챕터별 진척도 조회
    // GET /api/v1/lectures/{lectureId}/chapters/progress
    @Operation(summary = "챕터별 진척도 조회",
            description = "미시청 챕터는 watchedSeconds: 0, progressRate: 0, isCompleted: false 반환.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수강 권한 없음")
    })
    @GetMapping("/lectures/{lectureId}/chapters/progress")
    public ResponseEntity<ApiResponse<ChapterProgressResponse>> getChapterProgress(
            @Parameter(description = "강의 ID", required = true) @PathVariable("lectureId") Long lectureId,
            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.CHAPTER_PROGRESS_FOUND,
                "챕터별 진척도를 조회했습니다.",
                viewingQueryUseCase.getChapterProgress(userId, lectureId)
        ));

    }

    // 내 수강 강의 목록 조회
    // GET /api/v1/users/me/lectures
    @Operation(summary = "내 수강 강의 목록 조회",
            description = "수강 강의 없으면 lectures: [] 빈 배열 반환.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 토큰 만료")
    })
    @GetMapping("/user/me/lectures")
    public ResponseEntity<ApiResponse<MyLecturesResponse>> getMyLectures (
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

//        Long userId = 1L;
        Long userId = userDetails.getUserId();

        return ResponseEntity.ok(ApiResponse.success(
                ViewingResponseCode.MY_LECTURES_FOUND,
                "수강 강의 목록을 조회했습니다.",
                viewingQueryUseCase.getMyLectures(userId)
        ));

    }

}
