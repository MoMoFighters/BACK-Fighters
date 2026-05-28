package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.usecase.ChapterCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.presentation.api.request.CreateChapterRequest;
import com.wanted.momocity.lecture.presentation.api.request.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.request.RegisterChapterVideoRequest;
import com.wanted.momocity.lecture.presentation.api.response.CreateChapterResponse;
import com.wanted.momocity.lecture.presentation.api.response.CreateLectureResponse;
import com.wanted.momocity.lecture.presentation.api.response.RegisterChapterVideoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teacher/lectures")
@Tag(name = "Teacher Lecture", description = "강사용 강의 관리 API")
public class TeacherLectureController {

    private final LectureCommandUseCase lectureCommandUseCase;
    private final ChapterCommandUseCase chapterCommandUseCase;
    private final S3UploadPort s3UploadPort;
    public TeacherLectureController(
            LectureCommandUseCase lectureCommandUseCase,
            ChapterCommandUseCase chapterCommandUseCase,
            S3UploadPort s3UploadPort
    ) {
        this.lectureCommandUseCase = lectureCommandUseCase;
        this.chapterCommandUseCase = chapterCommandUseCase;
        this.s3UploadPort = s3UploadPort;
    }

    @Operation(
            summary = "강의 등록",
            description = "강사가 새로운 강의를 등록합니다. 썸네일 이미지는 multipart/form-data로 업로드합니다."
    )
    // Content-Type: multipart/form-data 즉, Json이 아닌 Form-data로 요청 받는다
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // 실행 전 권한 검증
    @PreAuthorize("hasAuthority('ROLE_TEACHER')") // 강사일 때만 이 API 가 호출 된다.
    public ResponseEntity<ApiResponse<CreateLectureResponse>> createLecture(
            Authentication authentication,
            @Valid @ModelAttribute CreateLectureRequest request
    ) {
        String teacherEmail = authentication.getName();

        /*
         * S3 업로드 전에 category를 먼저 검증
         * 잘못된 카테고리 요청이면 여기서 400 응답으로 끝나고, 썸네일 파일은 업로드 X
         */
        request.validateCategory();

        String thumbnailUrl = s3UploadPort.upload(request.thumbnail());

        LectureAggregate lecture = lectureCommandUseCase.createLecture(
                request.toCommand(teacherEmail, thumbnailUrl)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "강의가 등록되었습니다.",
                        CreateLectureResponse.from(lecture)
                ));
    }

    // 챕터 등록 API
    // 챕터 기본 정보만 JSON으로 받고, 동영상은 별도 API에서 form-data로 등록
    @Operation(
            summary = "챕터 등록",
            description = "강사가 새로운 챕터를 등록합니다. 챕터는 최소 1개는 무조건 넣어야 되며 최대 5개까지 추가할 수 있습니다."
    )
    @PostMapping("/{lectureId}/chapters")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<CreateChapterResponse>> createChapter(
            Authentication authentication,
            @PathVariable Long lectureId,
            @Valid @RequestBody CreateChapterRequest request
    ) {
        // Authorization 토큰에서 로그인한 강사의 email을 가져옴
        String teacherEmail = authentication.getName();

        // Request DTO를 application 계층의 Command로 변환
        var command = request.toCommand(teacherEmail, lectureId);

        // 챕터 등록 유스케이스를 실행
        LectureChapter chapter = chapterCommandUseCase.createChapter(command);

        // 201 Created 응답을 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "챕터가 등록되었습니다.",
                        CreateChapterResponse.from(chapter)
                ));
    }

    /*
     * 챕터 동영상 등록 API
     * 동영상 파일은 JSON이 아니라 multipart/form-data로 받는다.
     */
    @Operation(
            summary = "동영상 등록",
            description = "강사가 1개의 챕터에는 무조건 동영상을 추가해야 됩니다. 영상은 최대 500MB까지만 지원 가능합니다." +
                    "영상을 받을 때는 Form-data로 받습니다."
    )
    @PatchMapping(
            value = "/{lectureId}/chapters/{chapterId}/video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<RegisterChapterVideoResponse>> registerChapterVideo(
            Authentication authentication,
            @PathVariable Long lectureId,
            @PathVariable Long chapterId,
            @Valid @ModelAttribute RegisterChapterVideoRequest request
    ) {
        // Authorization 토큰에서 로그인한 강사의 email을 가져옵니다.
        String teacherEmail = authentication.getName();

        // 요청 DTO를 Application 계층에서 사용할 Command로 변환합니다.
        RegisterChapterVideoCommand command = request.toCommand(
                teacherEmail,
                lectureId,
                chapterId
        );

        // 챕터 동영상 등록 유스케이스를 실행합니다.
        LectureChapter chapter = chapterCommandUseCase.registerChapterVideo(command);

        // 200 OK 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "챕터 동영상이 등록되었습니다.",
                        RegisterChapterVideoResponse.from(chapter)
                ));
    }

}