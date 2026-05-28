package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.command.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.query.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.ChapterCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.presentation.api.request.ChangeLectureStatusRequest;
import com.wanted.momocity.lecture.presentation.api.request.CreateChapterRequest;
import com.wanted.momocity.lecture.presentation.api.request.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.request.RegisterChapterVideoRequest;
import com.wanted.momocity.lecture.presentation.api.response.*;
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
    private final LectureQueryUseCase lectureQueryUseCase;
    public TeacherLectureController(
            LectureCommandUseCase lectureCommandUseCase,
            ChapterCommandUseCase chapterCommandUseCase,
            S3UploadPort s3UploadPort,
            LectureQueryUseCase lectureQueryUseCase
    ) {
        this.lectureCommandUseCase = lectureCommandUseCase;
        this.chapterCommandUseCase = chapterCommandUseCase;
        this.s3UploadPort = s3UploadPort;
        this.lectureQueryUseCase = lectureQueryUseCase;
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

    // 강의 상태 변경 API
    @Operation(
            summary = "강의 상태 변경",
            description = """
                강사가 본인이 등록한 강의의 상태를 변경합니다.
                ACTIVE 상태로 변경하려면 챕터가 최소 1개 이상 있어야 하고,
                모든 챕터에 동영상이 등록되어 있어야 합니다.
                """
    )
    @PatchMapping("/{lectureId}/status")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<ChangeLectureStatusResponse>> changeLectureStatus(
            Authentication authentication,
            @PathVariable Long lectureId,
            @Valid @RequestBody ChangeLectureStatusRequest request
    ) {
        // Authorization 토큰에서 로그인한 강사의 email을 가져옴
        String teacherEmail = authentication.getName();

        // Request DTO를 Application 계층에서 사용할 Command로 변환
        ChangeLectureStatusCommand command = request.toCommand(
                teacherEmail,
                lectureId
        );

        // 강의 상태 변경 유스케이스를 실행
        LectureAggregate lecture = lectureCommandUseCase.changeLectureStatus(command);

        // 200 OK 응답을 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "강의 상태가 변경되었습니다.",
                        ChangeLectureStatusResponse.from(lecture)
                ));
    }

    /*
     * 강사 강의 목록 조회 API
     * 로그인한 강사가 본인이 등록한 강의 목록을 조회합니다.
     */
    @Operation(
            summary = "강사 강의 목록 조회",
            description = """
                로그인한 강사가 본인이 등록한 강의 목록을 조회합니다.
                category와 keyword는 선택 조건입니다.
                page는 1부터 시작합니다.
                """
    )
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<TeacherLecturePageResponse>> getTeacherLectures(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        // Authorization 토큰에서 로그인한 강사의 email을 가져옵니다.
        String teacherEmail = authentication.getName();

        // 요청 파라미터를 Application 계층의 Query 객체로 변환합니다.
        GetTeacherLecturesQuery query = new GetTeacherLecturesQuery(
                teacherEmail,
                page,
                size,
                parseCategory(category),
                keyword
        );

        // 강사 강의 목록 조회 유스케이스를 실행합니다.
        TeacherLecturePageResponse response = lectureQueryUseCase.getTeacherLectures(query);

        // 200 OK 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "강사 강의 목록 조회에 성공했습니다.",
                        response
                ));
    }

    /*
     * category 요청 파라미터를 LectureCategory enum으로 변환합니다.
     * category가 없으면 필터를 적용하지 않기 위해 null을 반환합니다.
     */
    private LectureCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        try {
            return LectureCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 카테고리입니다.");
        }
    }

}