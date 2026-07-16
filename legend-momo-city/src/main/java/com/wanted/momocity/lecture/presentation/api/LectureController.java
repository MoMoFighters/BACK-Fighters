package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.command.LectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.UpdateLectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteChapterVideoCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteChapterCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.DeleteLectureCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.ChangeLectureStatusCommand;
import com.wanted.momocity.lecture.application.command.LectureCommand.RegisterChapterVideoCommand;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetAdminLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetAdminLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetLecturesQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetStudentLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetTeacherLectureDetailQuery;
import com.wanted.momocity.lecture.application.query.LectureQuery.GetTeacherLecturesQuery;
import com.wanted.momocity.lecture.application.service.LectureS3UrlResolver;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCases.AdminLectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCases.ChapterCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCases.LectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCases.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCases.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.UpdateChapterRequest;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.UpdateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.AdminChangeLectureStatusRequest;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.ChangeLectureStatusRequest;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.RegisterChapterVideoRequest;
import com.wanted.momocity.lecture.presentation.api.request.LectureRequest.CreateChapterRequest;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminChangeLectureStatusResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.AdminLectureResponse.AdminLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.LectureResponse;
import com.wanted.momocity.lecture.presentation.api.response.LectureResponse.DeleteLectureResponse;
import com.wanted.momocity.lecture.presentation.api.response.LectureResponse.ChangeLectureStatusResponse;
import com.wanted.momocity.lecture.presentation.api.response.LectureResponse.CreateChapterResponse;
import com.wanted.momocity.lecture.presentation.api.response.LectureResponse.CreateLectureResponse;
import com.wanted.momocity.lecture.presentation.api.response.LectureResponse.RegisterChapterVideoResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.StudentLectureResponse.StudentLecturePageResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLectureDetailResponse;
import com.wanted.momocity.lecture.presentation.api.response.TeacherLectureResponse.TeacherLecturePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

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
    // 관리자 강의 조회 UseCase
    private final AdminLectureQueryUseCase adminLectureQueryUseCase;
    // 관리자 강의 상태 변경 UseCase
    private final AdminLectureCommandUseCase adminLectureCommandUseCase;

    private final LectureS3UrlResolver lectureS3UrlResolver;

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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "강의 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "썸네일 파일 크기 초과")
    })
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

        LectureAggregate lecture = lectureCommandUseCase.createLecture(
                request.toCommand(teacherId)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "강의가 등록되었습니다.",
                        CreateLectureResponse.from(lecture, lectureS3UrlResolver)
                ));
    }

    // 강의 수정
    @Operation(
            summary = "강의 수정",
            description = "강사가 본인 강의의 제목, 설명, 카테고리를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음 또는 본인 강의가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음")
    })
    @PatchMapping("/{lectureId}")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<Void>> updateLecture(
            Authentication authentication,
            @PathVariable Long lectureId,
            // Json으로 강의 수정 요청값 받음
            @Valid @RequestBody UpdateLectureRequest request
    ) {
        Long teacherId = Long.parseLong(authentication.getName());

        UpdateLectureCommand command = request.toCommand(
                teacherId,
                lectureId
        );

        lectureCommandUseCase.updateLecture(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의가 수정되었습니다.",
                null
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "챕터 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음 또는 본인 강의가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "챕터 순서 중복 또는 챕터 개수 제한 초과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "챕터 썸네일 파일 크기 초과")
    })
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

        request.validateThumbnailSize();

        LectureChapter chapter = chapterCommandUseCase.createChapter(
                request.toCommand(
                        teacherId,
                        lectureId
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "챕터가 등록되었습니다.",
                            CreateChapterResponse.from(chapter, lectureS3UrlResolver)
                ));
    }

    // 챕터 삭제
    @Operation(
            summary = "챕터 삭제",
            description = "강사가 본인 강의의 챕터를 삭제합니다. 챕터는 실제 row를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "챕터 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음 또는 본인 강의가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의 또는 챕터를 찾을 수 없음")
    })
    @DeleteMapping("/{lectureId}/chapters/{chapterId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            Authentication authentication,
            @PathVariable Long lectureId,
            @PathVariable Long chapterId
    ) {
        // 토큰에셔 아이디를 확인
        Long teacherId = Long.parseLong(authentication.getName());

        DeleteChapterCommand command = new DeleteChapterCommand(
                teacherId,
                lectureId,
                chapterId
        );

        // 챕터 삭제 비즈니스 로직 삭제
        chapterCommandUseCase.deleteChapter(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "챕터가 성공적으로 삭제되었습니다.",
                null
        ));
    }

    @Operation(
            summary = "챕터 수정",
            description = "강사가 본인 강의의 챕터 제목과 순서를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "챕터 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 챕터 순서 중복"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음 또는 본인 강의가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의 또는 챕터를 찾을 수 없음")
    })
    @PatchMapping("/{lectureId}/chapters/{chapterId}")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<Void>> updateChapter(
            Authentication authentication,
            @PathVariable Long lectureId,
            @PathVariable Long chapterId,
            @Valid @RequestBody UpdateChapterRequest request
    ) {
        Long teacherId = Long.parseLong(authentication.getName());
        LectureCommand.UpdateChapterCommand command = request.toCommand(
                teacherId,
                lectureId,
                chapterId
        );

        chapterCommandUseCase.updateChapter(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "챕터가 수정되었습니다.",
                null
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "챕터 동영상 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음 또는 본인 강의가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의 또는 챕터를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "동영상 파일 크기 초과")
    })
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
                RegisterChapterVideoResponse.from(chapter, lectureS3UrlResolver)
        ));
    }

    @Operation(
            summary = "챕터 동영상 삭제",
            description = "강사가 본인 강의의 챕터에 등록된 동영상을 삭제합니다. 영상은 챕터의 필수 요소이므로 해당 챕터도 함께 삭제됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "챕터 동영상 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 삭제할 동영상 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "강사 권한 없음 또는 본인 강의가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의 또는 챕터를 찾을 수 없음")
    })
    @DeleteMapping("/{lectureId}/chapters/{chapterId}/video")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteChapterVideo(
            Authentication authentication,
            @PathVariable Long lectureId,
            @PathVariable Long chapterId
    ) {
        Long teacherId = Long.parseLong(authentication.getName());

        DeleteChapterVideoCommand command = new DeleteChapterVideoCommand(
                teacherId,
                lectureId,
                chapterId
        );

        chapterCommandUseCase.deleteChapterVideo(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "챕터 동영상이 삭제되었습니다",
                null
        ));
    }

    // 온보딩 강의 통계 조회 API 정보를 표시합니다.
    @Operation(
            summary = "온보딩 강의 통계 조회",
            description = "토큰 없이 ACTIVE 강의 수와 ACTIVE 강의의 수강평 평균을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "온보딩 강의 통계 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/onboarding")
    public ResponseEntity<ApiResponse<LectureResponse.OnboardingLectureStatsResponse>> getOnboardingLectureStats() {
        LectureResponse.OnboardingLectureStatsResponse response = lectureQueryUseCase.getOnboardingLectureStats();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "온보딩 강의 통계 조회에 성공했습니다.",
                response
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 카테고리, 상태 또는 페이지 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLectures(
            Authentication authentication,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String role = getRoleOrAnonymous(authentication);
        Long userId = getUserIdOrNull(authentication);

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

        // 토큰 없음 또는 학생
        GetLecturesQuery query = new GetLecturesQuery(
                userId,
                parseCategory(category),
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 강의 식별자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음")
    })
    @GetMapping("/{lectureId}")
    public ResponseEntity<ApiResponse<?>> getLectureDetail(
            Authentication authentication,
            @PathVariable Long lectureId
    ) {
        // 토큰이 있음녀 ROLE값, 없으면 Anonymous로 처리
        String role = getRoleOrAnonymous(authentication);
        // 토큰이 있으면 userId, 없으면 null 값
        Long userId = getUserIdOrNull(authentication);

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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "허용되지 않은 상태 변경 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "상태 변경 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "강의 상태 변경 조건 불충족")
    })
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


    @Operation(
            summary = "강의 삭제",
            description = "강사 또는 관리자가 강의를 삭제합니다. 실제 삭제가 아니라 강의 상태를 DELETED로 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강의 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 삭제된 강의")
    })
    @DeleteMapping("/{lectureId}")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DeleteLectureResponse>> deleteLecture(
            Authentication authentication,
            @PathVariable Long lectureId
    ) {
        // 토큰에서 요청자 ID 확인
        Long userId= Long.parseLong(authentication.getName());

        // 토큰에서 강사인지 관리자인지 확인
        String role = getRole(authentication);

        DeleteLectureCommand command = new DeleteLectureCommand(
                userId,
                role,
                lectureId
        );

        // 삭제 UseCase 실행
        LectureAggregate lecture = lectureCommandUseCase.deleteLecture(command);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의가 삭제되었습니다.",
                DeleteLectureResponse.from(lecture)
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

    private String getRoleOrAnonymous(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "ANONYMOUS";
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ANONYMOUS");
    }

    private Long getUserIdOrNull(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        return Long.parseLong(authentication.getName());
    }


}
