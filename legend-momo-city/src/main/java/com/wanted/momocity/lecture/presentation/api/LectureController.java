package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.command.ChangeChapterVideoStatusCommand;
import com.wanted.momocity.lecture.application.command.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.query.GetAdminLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.GetStudentLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.AdminLectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.application.usecase.ChapterCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.presentation.api.request.AdminChangeLectureStatusRequest;
import com.wanted.momocity.lecture.presentation.api.request.ChangeChapterVideoStatusRequest;
import com.wanted.momocity.lecture.presentation.api.request.ChangeLectureStatusRequest;
import com.wanted.momocity.lecture.presentation.api.request.CreateChapterRequest;
import com.wanted.momocity.lecture.presentation.api.request.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.request.RegisterChapterVideoRequest;
import com.wanted.momocity.lecture.presentation.api.response.AdminChangeLectureStatusResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.ChangeLectureStatusResponse;
import com.wanted.momocity.lecture.presentation.api.response.CreateChapterResponse;
import com.wanted.momocity.lecture.presentation.api.response.CreateLectureResponse;
import com.wanted.momocity.lecture.presentation.api.response.RegisterChapterVideoResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLecturePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@Tag(name = "Lecture", description = "강의 등록, 조회, 챕터, 영상, 상태 관리 API")
public class LectureController {

    // 강의 조회 UseCase
    private final LectureQueryUseCase lectureQueryUseCase;
    // 강의 상태 변경(WAITING) UseCase
    private final LectureCommandUseCase lectureCommandUseCase;
    // 챕터 UseCase
    private final ChapterCommandUseCase chapterCommandUseCase;
    // S3 업로드 Port
    private final S3UploadPort s3UploadPort;
    // 관리자 강의 조회 UseCase
    private final AdminLectureQueryUseCase adminLectureQueryUseCase;
    // 관리자 강의 상태 변경 UseCase
    private final AdminLectureCommandUseCase adminLectureCommandUseCase;


    /* comment
     * 강의 등록 API
     *
     * URL에 role을 넣지 않는 정책에 따라 /api/v1/lectures에서 처리한다.
     * 썸네일 파일을 함께 받기 때문에 multipart/form-data로 요청을 받는다.
     *
     * 처리 흐름:
     * 1. 로그인한 강사 ID를 토큰에서 꺼낸다.
     * 2. S3 업로드 전에 카테고리와 썸네일 크기를 검증한다.
     * 3. 썸네일 파일을 S3에 업로드하고 URL을 받는다.
     * 4. Application 계층에 강의 생성 Command를 전달한다.
     * 5. 생성된 강의를 201 Created로 응답한다.
     */
    @Operation(
            summary = "강의 등록",
            description = "강사가 강의를 등록합니다. 썸네일 파일을 포함하므로 multipart/form-data로 요청합니다."
    )
    @PostMapping(
            value = "",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<CreateLectureResponse>> createLecture(
            Authentication authentication,
            @Valid @ModelAttribute CreateLectureRequest request
    ) {
        Long teacherId = Long.parseLong(authentication.getName());

        // S3 업로드 전에 입력값을 먼저 검증해서 실패 요청의 파일 업로드를 막는다.
        request.validateCategory();
        request.validateThumbnailSize();

        String thumbnailUrl = s3UploadPort.upload(request.thumbnail());

        LectureAggregate lecture = lectureCommandUseCase.createLecture(
                request.toCommand(teacherId, thumbnailUrl)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "강의가 등록되었습니다.",
                        CreateLectureResponse.from(lecture)
                ));
    }

    /* comment
     * 챕터 등록 API
     *
     * 프론트 통합 등록 흐름에 맞춰 multipart/form-data로 받는다.
     * 현재 챕터 등록 자체는 title, orderNo만 사용하고,
     * 실제 동영상 파일은 별도 동영상 등록 API에서 처리한다.
     *
     * 처리 흐름:
     * 1. 로그인한 강사 ID를 꺼낸다.
     * 2. lectureId와 요청값을 CreateChapterCommand로 변환한다.
     * 3. 챕터 등록 UseCase를 실행한다.
     * 4. 생성된 챕터를 201 Created로 응답한다.
     */
    @Operation(
            summary = "챕터 등록",
            description = "강사가 본인 강의에 챕터를 등록합니다. 프론트 통합 등록 흐름에 맞춰 multipart/form-data로 요청합니다."
    )
    @PostMapping(
            value = "/{lectureId}/chapters",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<CreateChapterResponse>> createChapter(
            Authentication authentication,
            @PathVariable Long lectureId,
            @Valid @ModelAttribute CreateChapterRequest request
    ) {
        Long teacherId = Long.parseLong(authentication.getName());

        LectureChapter chapter = chapterCommandUseCase.createChapter(
                request.toCommand(teacherId, lectureId)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "챕터가 등록되었습니다.",
                        CreateChapterResponse.from(chapter)
                ));
    }

    /* comment
     * 챕터 동영상 등록 API
     *
     * 하나의 챕터에 동영상 파일을 연결한다.
     * 영상 파일을 받기 때문에 multipart/form-data로 요청을 받는다.
     *
     * 처리 흐름:
     * 1. 로그인한 강사 ID를 꺼낸다.
     * 2. lectureId, chapterId, video 파일 정보를 Command로 변환한다.
     * 3. Application 계층에서 소유자 검증, 파일 크기 검증, S3 업로드를 처리한다.
     * 4. 동영상 정보가 채워진 챕터 정보를 응답한다.
     */
    @Operation(
            summary = "챕터 동영상 등록",
            description = "강사가 본인 강의의 챕터에 동영상을 등록합니다. 영상 파일을 포함하므로 multipart/form-data로 요청합니다."
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
        Long teacherId = Long.parseLong(authentication.getName());

        RegisterChapterVideoCommand command = request.toCommand(
                teacherId,
                lectureId,
                chapterId
        );

        LectureChapter chapter = chapterCommandUseCase.registerChapterVideo(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "챕터 동영상이 등록되었습니다.",
                RegisterChapterVideoResponse.from(chapter)
        ));
    }

    /* comment
     * 강의 목록 조회 API
     *
     * 같은 URL을 사용하지만 로그인 사용자의 ROLE에 따라 조회 정책이 달라진다.
     *
     * ROLE_ADMIN:
     * - 관리자 강의 목록 조회
     * - WAITING, ACTIVE 상태 강의 중심으로 조회
     *
     * ROLE_TEACHER:
     * - 로그인한 강사가 등록한 본인 강의 목록 조회
     *
     * ROLE_STUDENT:
     * - 학생용 강의 목록 조회
     * - ACTIVE 강의만 조회
     * - 수강 여부(isEnrolled)를 포함한다.
     */
    @Operation(
            summary = "강의 목록 조회",
            description = "로그인 사용자의 권한에 따라 학생, 강사, 관리자 기준 강의 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLectures(
            Authentication authentication,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean enrolled,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String role = getRole(authentication);
        Long userId = Long.parseLong(authentication.getName());

        if ("ROLE_ADMIN".equals(role)) {
            GetAdminLecturesQuery query = new GetAdminLecturesQuery(
                    userId,
                    parseStatus(status),
                    parseCategory(category),
                    keyword,
                    page,
                    size
            );

            AdminLecturePageResponse response = adminLectureQueryUseCase.getAdminLectures(query);

            return ResponseEntity.ok(ApiResponse.success(
                    ApiResponseCode.SUCCESS,
                    "관리자 강의 목록 조회에 성공했습니다.",
                    response
            ));
        }

        if ("ROLE_TEACHER".equals(role)) {
            GetTeacherLecturesQuery query = new GetTeacherLecturesQuery(
                    userId,
                    page,
                    size,
                    parseCategory(category),
                    keyword
            );

            TeacherLecturePageResponse response = lectureQueryUseCase.getTeacherLectures(query);

            return ResponseEntity.ok(ApiResponse.success(
                    ApiResponseCode.SUCCESS,
                    "강사 강의 목록 조회에 성공했습니다.",
                    response
            ));
        }

        GetLecturesQuery query = new GetLecturesQuery(
                userId,
                parseCategory(category),
                enrolled,
                keyword,
                page,
                size
        );

        StudentLecturePageResponse response = lectureQueryUseCase.getLectures(query);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의 목록 조회에 성공했습니다.",
                response
        ));
    }

    /* comment
     * 강의 상세 조회 API
     *
     * 같은 URL을 사용하지만 ROLE에 따라 응답 데이터와 접근 범위가 달라진다.
     *
     * ROLE_ADMIN:
     * - 관리자 강의 상세 조회
     * - 승인 대기/진행 중 강의와 챕터, 영상 상태를 확인한다.
     *
     * ROLE_TEACHER:
     * - 강사 본인 강의 상세 조회
     * - 본인이 등록한 강의만 조회할 수 있다.
     *
     * ROLE_STUDENT:
     * - 학생용 강의 상세 조회
     * - ACTIVE 상태 강의만 조회한다.
     * - 수강 여부와 READY 상태 챕터 정보를 내려준다.
     */
    @Operation(
            summary = "강의 상세 조회",
            description = "로그인 사용자의 권한에 따라 학생, 강사, 관리자 기준 강의 상세 정보를 조회합니다."
    )
    @GetMapping("/{lectureId}")
    public ResponseEntity<ApiResponse<?>> getLectureDetail(
            Authentication authentication,
            @PathVariable Long lectureId
    ) {
        Long userId = Long.parseLong(authentication.getName());
        String role = getRole(authentication);

        if ("ROLE_ADMIN".equals(role)) {
            GetAdminLectureDetailQuery query = new GetAdminLectureDetailQuery(
                    userId,
                    lectureId
            );

            AdminLectureDetailResponse response =
                    adminLectureQueryUseCase.getAdminLectureDetail(query);

            return ResponseEntity.ok(ApiResponse.success(
                    ApiResponseCode.SUCCESS,
                    "관리자 강의 상세 조회에 성공했습니다.",
                    response
            ));
        }

        if ("ROLE_TEACHER".equals(role)) {
            GetTeacherLectureDetailQuery query = new GetTeacherLectureDetailQuery(
                    userId,
                    lectureId
            );

            TeacherLectureDetailResponse response =
                    lectureQueryUseCase.getTeacherLectureDetail(query);

            return ResponseEntity.ok(ApiResponse.success(
                    ApiResponseCode.SUCCESS,
                    "강사 강의 상세 조회에 성공했습니다.",
                    response
            ));
        }

        GetStudentLectureDetailQuery query = new GetStudentLectureDetailQuery(
                userId,
                lectureId
        );

        StudentLectureDetailResponse response =
                lectureQueryUseCase.getStudentLectureDetail(query);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의 상세 조회에 성공했습니다.",
                response
        ));
    }

    /* comment
     * 강의 상태 변경 API
     *
     * 같은 URL을 사용하지만 ROLE에 따라 상태 변경 정책이 다르다.
     *
     * ROLE_TEACHER:
     * - 본인 강의를 검수 요청 상태(WAITING)로만 변경할 수 있다.
     * - 챕터가 최소 1개 이상 있어야 한다.
     * - 모든 챕터에 동영상이 등록되어 있어야 한다.
     *
     * ROLE_ADMIN:
     * - 강의를 승인(ACTIVE) 또는 거절(HOLD)할 수 있다.
     * - ACTIVE 승인 시 모든 동영상이 READY 상태인지 검증한다.
     */
    @Operation(
            summary = "강의 상태 변경",
            description = """
                    강사는 본인 강의를 검수 요청(WAITING) 상태로 변경합니다.
                    관리자는 강의를 승인(ACTIVE) 또는 거절(HOLD) 상태로 변경합니다.
                    """
    )
    @PatchMapping("/{lectureId}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<?>> changeLectureStatus(
            Authentication authentication,
            @PathVariable Long lectureId,
            @Valid @RequestBody ChangeLectureStatusRequest request
    ) {
        Long userId = Long.parseLong(authentication.getName());
        String role = getRole(authentication);

        if ("ROLE_ADMIN".equals(role)) {
            AdminChangeLectureStatusRequest adminRequest =
                    new AdminChangeLectureStatusRequest(request.lectureStatus());

            AdminChangeLectureStatusResponse response =
                    adminLectureCommandUseCase.changeLectureStatus(
                            adminRequest.toCommand(userId, lectureId)
                    );

            return ResponseEntity.ok(ApiResponse.success(
                    ApiResponseCode.SUCCESS,
                    "강의 상태가 변경되었습니다.",
                    response
            ));
        }

        ChangeLectureStatusCommand command = request.toCommand(
                userId,
                lectureId
        );

        LectureAggregate lecture = lectureCommandUseCase.changeLectureStatus(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의 상태가 변경되었습니다.",
                ChangeLectureStatusResponse.from(lecture)
        ));
    }

    /*
     * 챕터 동영상 상태 변경 API
     *
     * 업로드된 동영상의 처리 상태를 변경한다.
     * 실제 파일 업로드가 아니라 상태값만 변경하므로 JSON으로 받는다.
     *
     * 예:
     * - UPLOADING
     * - ENCODING
     * - READY
     * - FAILED
     *
     * READY 상태가 되면 학생 화면에서 재생 가능한 영상으로 판단할 수 있다.
     */
    @Operation(
            summary = "챕터 동영상 상태 변경",
            description = "강사가 본인 강의의 챕터 동영상 상태를 변경합니다."
    )
    @PatchMapping("/{lectureId}/chapters/{chapterId}/video/status")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<RegisterChapterVideoResponse>> changeChapterVideoStatus(
            Authentication authentication,
            @PathVariable Long lectureId,
            @PathVariable Long chapterId,
            @Valid @RequestBody ChangeChapterVideoStatusRequest request
    ) {
        Long teacherId = Long.parseLong(authentication.getName());

        ChangeChapterVideoStatusCommand command = request.toCommand(
                teacherId,
                lectureId,
                chapterId
        );

        LectureChapter chapter = chapterCommandUseCase.changeChapterVideoStatus(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "챕터 동영상 상태가 변경되었습니다.",
                RegisterChapterVideoResponse.from(chapter)
        ));
    }

    /* comment
     * Authentication에서 ROLE 값을 꺼낸다.
     * 같은 URL에서 학생/강사/관리자 흐름을 분기하기 위해 사용한다.
     */
    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElseThrow(() -> new DomainRuleViolationException("사용자 권한 정보가 없습니다."));
    }

    /* comment
     * 관리자 목록 조회에서 사용하는 status query parameter를 LectureStatus enum으로 변환한다.
     * 값이 없으면 null을 반환해 전체 상태 조건으로 처리한다.
     */
    private LectureStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return LectureStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 상태입니다.");
        }
    }


    /* comment
     * category query parameter를 LectureCategory enum으로 변환한다.
     * 값이 없으면 null을 반환해 카테고리 필터를 적용하지 않는다.
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
