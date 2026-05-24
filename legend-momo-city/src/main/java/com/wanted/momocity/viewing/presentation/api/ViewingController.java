package com.wanted.momocity.viewing.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.application.usecase.*;
import com.wanted.momocity.viewing.presentation.api.common.ViewingResponseCode;
import com.wanted.momocity.viewing.presentation.api.request.SaveProgressRequest;
import com.wanted.momocity.viewing.presentation.api.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
* comment.
*  HTTP 요청을 받아서 UseCase 에 전달하고 응답 반환
*  비지니스 로직 없음, HTTP 반환만 담당
* */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ViewingController {

    private final GetStreamingUrlUseCase getStreamingUrlUseCase;
    private final GetLectureMetaUseCase getLectureMetaUseCase;
    private final SaveProgressUseCase saveProgressUseCase;
    private final GetChapterResumeUseCase getChapterResumeUseCase;
    private final GetTotalProgressUseCase getTotalProgressUseCase;
    private final GetChapterProgressUseCase getChapterProgressUseCase;
    private final GetMyLectureUseCase getMyLectureUseCase;

    // S3 Presigned URL 발급
    // GET /api/v1/lectures/{lectureId}/chapters/{chapterId}/stream
    @GetMapping("/lectures/{lectureId}/chapters/{chapterId}/stream")
    public ResponseEntity<ApiResponse<StreamingUrlResponse>> getStreamingUrl (
            @PathVariable Long lectureId,
            @PathVariable Long chapterId
    ) {

        // JWT 완성 후 @AuthenticationPrincipal 교체 예정
        Long userId = 1L;

        StreamingUrlResponse response = getStreamingUrlUseCase
                .getStreamingUrl(userId, lectureId, chapterId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.STREAMING_URL_ISSUED,
                        "영상 스트리밍 URL 이 발급되었습니다.",
                        response
                )
        );

    }

    // 강의 메타데이터 조회
    // GET /api/v1/lectures/{lectureId}
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<ApiResponse<LectureMetaResponse>> getLectureMeta(
            @PathVariable Long lectureId
    ) {

        Long userId = 1L;

        LectureMetaResponse response = getLectureMetaUseCase
                .getLectureMeta(userId, lectureId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.LECTURE_META_FOUND,
                        "강의 메타데이터 조회에 성공했습니다.",
                        response
                )
        );

    }

    // 강의 영상 재생 진척도 저장
    // PATCH /api/v1/lectures/{lectureId}/chapters/{chapterId}/progress
    @PatchMapping("/lectures/{lectureId}/chapters/{chapterId}/progress")
    public ResponseEntity<ApiResponse<SaveProgressResponse>> saveProgress(
            @PathVariable Long lectureId,
            @PathVariable Long chapterId,
            @RequestBody @Valid SaveProgressRequest request
            ) {

        Long userId = 1L;

        SaveProgressCommand command = new SaveProgressCommand(
                userId,
                lectureId,
                chapterId,
                request.playbackSeconds()
        );

        SaveProgressResponse response = saveProgressUseCase
                .saveProgress(command);

        return  ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.PROGRESS_SAVED,
                        "진척도가 업데이트되었습니다.",
                        response
                )
        );

    }

    // 챕터 이어보기
    // GET /api/v1/lectures/{lectureId}/chapters/{chapterId}/resume
    @GetMapping("/lectures/{lectureId}/chapters/{chapterId}/resume")
    public ResponseEntity<ApiResponse<ChapterResumeResponse>> getChapterResume(
            @PathVariable Long lectureId,
            @PathVariable Long chapterId
    ) {

        Long userId = 1L;

        ChapterResumeResponse response = getChapterResumeUseCase
                .getChapterResume(userId, lectureId, chapterId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.CHAPTER_RESUME_FOUND,
                        "챕터 이어보기 정보를 조회했습니다.",
                        response
                )
        );

    }

    // 전체 진척도 조회
    // GET /api/v1/lectures/{lectureId}/progress
    @GetMapping("/lectures/{lectureId}/progress")
    public ResponseEntity<ApiResponse<TotalProgressResponse>> getTotalProgress(
            @PathVariable Long lectureId
    ) {

        Long userId = 1L;

        TotalProgressResponse response = getTotalProgressUseCase
                .getTotalProgress(userId, lectureId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.TOTAL_PROGRESS_FOUND,
                        "전체 진척도를 조회했습니다.",
                        response
                )
        );
    }

    // 챕터별 진척도 조회
    // GET /api/v1/lectures/{lectureId}/chapters/progress
    @GetMapping("/lectures/{lectureId}/chapters/progress")
    public ResponseEntity<ApiResponse<ChapterProgressResponse>> getChapterProgress(
            @PathVariable Long lectureId
    ) {

        Long userId = 1L;

        ChapterProgressResponse response = getChapterProgressUseCase
                .getChapterProgress(userId, lectureId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.CHAPTER_PROGRESS_FOUND,
                        "챕터별 진척도를 조회했습니다.",
                        response
                )
        );

    }

    // 내 수강 강의 목록 조회
    // GET /api/v1/users/me/lectures
    @GetMapping("/users/me/lectures")
    public ResponseEntity<ApiResponse<List<MyLectureResponse>>> getMyLectures () {

        Long userId = 1L;

        List<MyLectureResponse> responses = getMyLectureUseCase
                .getMyLectures(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ViewingResponseCode.MY_LECTURES_FOUND,
                        "수강 강의 목록을 조회했습니다.",
                        responses
                )
        );

    }

}
