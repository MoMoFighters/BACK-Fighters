package com.wanted.momocity.teacher.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.teacher.application.command.ApproveTeacherCommand;
import com.wanted.momocity.teacher.application.command.RejectTeacherCommand;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationCommandUseCase;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationCommandUseCase.TeacherActionResult;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationQueryUseCase;
import com.wanted.momocity.teacher.application.usecase.TeacherApplicationQueryUseCase.TeacherApplicationListResult;
import com.wanted.momocity.teacher.domain.model.TeacherApplication;
import com.wanted.momocity.teacher.presentation.api.request.TeacherActionRequest;
import com.wanted.momocity.teacher.presentation.api.response.TeacherActionResponse;
import com.wanted.momocity.teacher.presentation.api.response.TeacherApplicationDetailResponse;
import com.wanted.momocity.teacher.presentation.api.response.TeacherApplicationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 강사 신청자 조회 및 승인/반려 컨트롤러.
 *
 * 다루는 API (모두 모듈03):
 *  - MS-3: GET  /api/v1/admin/users/instructor-applications
 *  - MS-4: GET  /api/v1/admin/users/instructor-applications/{userId}
 *  - MS-5: PATCH /api/v1/admin/users/{userId}/role
 *
 * REF: module00-clean-architecture catalog/presentation/api/CourseController.java
 *
 * 권한: 모든 엔드포인트 ADMIN 만 접근 가능 (@PreAuthorize). 인증/인가 팀 PR 머지 후
 *      hasRole('ADMIN') vs hasAuthority('ROLE_ADMIN') 컨벤션 1회 점검 필요.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Teacher - 강사 신청 관리", description = "강사 신청자 조회 및 승인/반려 (MS-3, MS-4, MS-5)")
public class TeacherApplicationController {

    private final TeacherApplicationQueryUseCase queryUseCase;
    private final TeacherApplicationCommandUseCase commandUseCase;

    public TeacherApplicationController(
            TeacherApplicationQueryUseCase queryUseCase,
            TeacherApplicationCommandUseCase commandUseCase
    ) {
        this.queryUseCase = queryUseCase;
        this.commandUseCase = commandUseCase;
    }

    @GetMapping("/users/instructor-applications")
    @Operation(summary = "강사 신청자 목록 조회 (MS-3)")
    public ResponseEntity<ApiResponse<TeacherApplicationListResponse>> getApplicationList(
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        TeacherApplicationListResult result = queryUseCase.getApplicationList(page, size);
        TeacherApplicationListResponse response = new TeacherApplicationListResponse(
                result.applications().stream().map(this::toItem).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(
                TeacherResponseCode.APPLICATION_LIST_FETCHED,
                TeacherResponseMessage.APPLICATION_LIST_FETCHED,
                response
        ));
    }

    @GetMapping("/users/instructor-applications/{userId}")
    @Operation(summary = "강사 신청자 상세 조회 (MS-4)")
    public ResponseEntity<ApiResponse<TeacherApplicationDetailResponse>> getApplicationDetail(
            @Parameter(description = "신청자 user PK", example = "7")
            @PathVariable Long userId
    ) {
        TeacherApplication application = queryUseCase.getApplicationDetail(userId);
        TeacherApplicationDetailResponse response = toDetailResponse(application);
        return ResponseEntity.ok(ApiResponse.success(
                TeacherResponseCode.APPLICATION_DETAIL_FETCHED,
                TeacherResponseMessage.APPLICATION_DETAIL_FETCHED,
                response
        ));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "강사 승인/반려 (MS-5)",
            description = "action=APPROVE 시 강사 승인, action=REJECT 시 반려. REJECT 는 reason 최소 10자.")
    public ResponseEntity<ApiResponse<TeacherActionResponse>> changeRole(
            @Parameter(description = "신청자 user PK", example = "7")
            @PathVariable Long userId,
            @Valid @RequestBody TeacherActionRequest request
    ) {
        TeacherActionResult result = "APPROVE".equals(request.action())
                ? commandUseCase.approve(new ApproveTeacherCommand(userId))
                : commandUseCase.reject(new RejectTeacherCommand(userId, request.reason()));

        TeacherActionResponse response = new TeacherActionResponse(
                result.userId(),
                result.status(),
                result.reason(),
                result.processedAt()
        );
        boolean approved = "APPROVE".equals(request.action());
        return ResponseEntity.ok(ApiResponse.success(
                approved ? TeacherResponseCode.APPROVED : TeacherResponseCode.REJECTED,
                approved ? TeacherResponseMessage.APPROVED : TeacherResponseMessage.REJECTED,
                response
        ));
    }

    private TeacherApplicationListResponse.Item toItem(TeacherApplication app) {
        return new TeacherApplicationListResponse.Item(
                app.userId(),
                app.nickname(),
                app.name(),
                app.email(),
                app.category(),
                app.appliedAt()
        );
    }

    private TeacherApplicationDetailResponse toDetailResponse(TeacherApplication app) {
        return new TeacherApplicationDetailResponse(
                app.userId(),
                app.nickname(),
                app.name(),
                app.email(),
                app.birth(),
                app.profileImageUrl(),
                app.category(),
                app.proof(),
                app.appliedAt()
        );
    }
}
