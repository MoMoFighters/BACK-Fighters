package com.wanted.cleanarchitecture.catalog.presentation.api;

import com.wanted.cleanarchitecture.catalog.application.command.AddModuleCommand;
import com.wanted.cleanarchitecture.catalog.application.command.AddSectionCommand;
import com.wanted.cleanarchitecture.catalog.application.command.CreateCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.command.PublishCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.usecase.CourseCommandUseCase;
import com.wanted.cleanarchitecture.catalog.application.usecase.CourseQueryUseCase;
import com.wanted.cleanarchitecture.catalog.presentation.api.request.AddModuleRequest;
import com.wanted.cleanarchitecture.catalog.presentation.api.request.AddSectionRequest;
import com.wanted.cleanarchitecture.catalog.presentation.api.request.CreateCourseRequest;
import com.wanted.cleanarchitecture.catalog.presentation.api.response.CreateCourseResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseCode;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * CourseController는 Presentation 계층의 HTTP Adapter다.
 *
 * 계층별 호출 흐름:
 * HTTP Request
 *   -> CourseController
 *      -> Request DTO를 Application Command로 변환
 *      -> CourseCommandUseCase 또는 CourseQueryUseCase 호출
 *      -> ApiResponse로 HTTP 응답 변환
 *
 * Controller가 하지 않는 일:
 * - 강의를 공개할 수 있는지 판단하지 않는다.
 * - JPA Entity를 직접 만들거나 저장하지 않는다.
 * - DomainEvent를 직접 발행하지 않는다.
 *
 * 즉 Controller는 "웹 요청을 애플리케이션 요청으로 번역하는 역할"에 집중한다.
 */
@RestController
@RequestMapping("/api/courses")
@Tag(name = "Catalog - Course", description = "강의 생성, 섹션/모듈 구성, 강의 공개를 처리하는 Catalog Context API")
public class CourseController {

    private final CourseCommandUseCase courseCommandUseCase;
    private final CourseQueryUseCase courseQueryUseCase;

    public CourseController(
            CourseCommandUseCase courseCommandUseCase,
            CourseQueryUseCase courseQueryUseCase
    ) {
        this.courseCommandUseCase = courseCommandUseCase;
        this.courseQueryUseCase = courseQueryUseCase;
    }

    @GetMapping("/{courseId}")
    @Operation(
            summary = "강의 단건 조회",
            description = "Catalog Context의 Course 조회 결과를 API 응답 모델로 반환한다."
    )
    public ResponseEntity<ApiResponse<CourseQueryUseCase.CourseView>> getCourse(
            @Parameter(description = "조회할 강의 ID", example = "1")
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                courseQueryUseCase.handle(courseId)
        ));
    }

    @PostMapping
    @Operation(
            summary = "강의 생성",
            description = "교육자가 새로운 강의 초안을 생성한다. EventStorming의 CreateCourseCommand에 해당하는 요청이다."
    )
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        Long courseId = courseCommandUseCase.handle(new CreateCourseCommand(
                request.authorId(),
                request.title(),
                request.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.COURSE_CREATED,
                        ApiResponseMessage.COURSE_CREATED,
                        new CreateCourseResponse(courseId)
                ));
    }

    @PostMapping("/{courseId}/sections")
    @Operation(
            summary = "강의 섹션 추가",
            description = "초안 상태의 강의에 섹션을 추가한다. 섹션 순서 중복 여부는 Course Aggregate가 검증한다."
    )
    public ResponseEntity<ApiResponse<Void>> addSection(
            @Parameter(description = "섹션을 추가할 강의 ID", example = "1")
            @PathVariable Long courseId,
            @Valid @RequestBody AddSectionRequest request
    ) {
        courseCommandUseCase.handle(new AddSectionCommand(courseId, request.title(), request.sectionOrder()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.COURSE_SECTION_CREATED,
                        ApiResponseMessage.COURSE_SECTION_CREATED,
                        null
                ));
    }

    @PostMapping("/{courseId}/sections/{sectionOrder}/modules")
    @Operation(
            summary = "강의 모듈 추가",
            description = "특정 섹션에 학습 모듈을 추가한다. 모듈 순서 중복 여부는 CourseSection이 검증한다."
    )
    public ResponseEntity<ApiResponse<Void>> addModule(
            @Parameter(description = "모듈을 추가할 강의 ID", example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "모듈을 추가할 섹션 순서", example = "1")
            @PathVariable int sectionOrder,
            @Valid @RequestBody AddModuleRequest request
    ) {
        courseCommandUseCase.handle(new AddModuleCommand(
                courseId,
                sectionOrder,
                request.title(),
                request.contentType(),
                request.moduleOrder()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.COURSE_MODULE_CREATED,
                        ApiResponseMessage.COURSE_MODULE_CREATED,
                        null
                ));
    }

    @PostMapping("/{courseId}/publication")
    @Operation(
            summary = "강의 공개",
            description = "강의를 공개 상태로 변경한다. 공개 규칙은 Course.publish()가 검증하고, 성공 시 CoursePublishedEvent가 기록된다."
    )
    public ResponseEntity<ApiResponse<Void>> publishCourse(
            @Parameter(description = "공개할 강의 ID", example = "1")
            @PathVariable Long courseId
    ) {
        courseCommandUseCase.handle(new PublishCourseCommand(courseId));
        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.COURSE_PUBLISHED,
                ApiResponseMessage.COURSE_PUBLISHED
        ));
    }
}
