package com.wanted.momocity.user.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.user.application.command.TeacherApplyCommand;
import com.wanted.momocity.auth.domain.exception.MissingProofException;
import com.wanted.momocity.user.presentation.api.request.TeacherApplyRequest;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.user.application.command.ApproveTeacherCommand;
import com.wanted.momocity.user.application.command.RejectTeacherCommand;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase.TeacherActionResult;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase.TeacherApplicationListResult;
import com.wanted.momocity.user.presentation.api.response.TeacherResponseCode;
import com.wanted.momocity.user.presentation.api.response.TeacherResponseMessage;
import com.wanted.momocity.user.presentation.api.request.TeacherActionRequest;
import com.wanted.momocity.user.presentation.api.response.TeacherActionResponse;
import com.wanted.momocity.user.presentation.api.response.TeacherApplicationDetailResponse;
import com.wanted.momocity.user.presentation.api.response.TeacherApplicationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/* comment.
    TeacherApplicationController 정리
    1. 해당 클래스가 하는 일 : 강사 신청자 조회 및 승인/반려 HTTP API 진입점
    2. 다루는 API 3개
        - MS-3 : GET  /api/v1/admin/users/instructor-applications
        - MS-4 : GET  /api/v1/admin/users/instructor-applications/{userId}
        - MS-5 : PATCH /api/v1/admin/users/{userId}/role
    3. 클래스 레벨 애노테이션 :
        - @RestController : REST API 컨트롤러
        - @RequestMapping("/api/v1/admin") : 공통 prefix
        - @PreAuthorize("hasRole('ADMIN')") : 모든 핸들러 ADMIN 권한 필수
        - @Tag : Swagger 문서화
    4. 의존성 2개 :
        - TeacherApplicationQueryUseCase : 조회용 (MS-3/4)
        - TeacherApplicationCommandUseCase : 변경용 (MS-5)
        - 인터페이스 타입 의존 (DIP)
    5. Controller 의 책임 :
        - HTTP 요청 → Command/Query 변환
        - UseCase 호출
        - 도메인 결과 → Response 변환
        - ApiResponse 로 감싸서 반환
    6. Controller 가 *하지 않는 일* :
        - 비즈니스 로직 (UseCase/Service 책임)
        - DB 직접 접근 (Repository/Adapter 책임)
        - 도메인 검증 (Member/도메인 모델 책임)
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Teacher - 강사 신청 관리", description = "강사 신청자 조회 및 승인/반려 (MS-3, MS-4, MS-5)")
public class TeacherApplicationController {

    private final UserCommandUsecase userCommandUsecase;
    private final UserQueryUsecase userQueryUsecase;

    private final S3UploadPort s3UploadPort;



    @PostMapping("/teacherApply")
    @Operation(
            summary = "강사 신청",
            description = "강사는 해당 api를 통해 강사 증빙자료를 제출하고 강사 신청을 진행한다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 신청 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (@Valid 실패)"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ResponseEntity<ApiResponse<Void>> teacherApply(
            @Valid @ModelAttribute TeacherApplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        // 증빙자료 제출 여부 확인
        if (request.proof() == null || request.proof().isEmpty()) {
            throw new MissingProofException("증빙 자료는 필수 제출입니다.");
        }

        String proofKey = s3UploadPort.upload(request.proof(),"teacher_proof");

        userCommandUsecase.teacherApply(new TeacherApplyCommand(userDetails.getUserId(),request.nickname(),request.category(),proofKey));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        TeacherResponseCode.SUCCESS,
                        TeacherResponseMessage.TEACHER_APPLIED,
                        null
                ));
    }

    @GetMapping("/admin/users/instructor-applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "강사 신청자 목록 조회 (MS-3)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 신청자 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public ResponseEntity<ApiResponse<TeacherApplicationListResponse>> getApplicationList(
            @Parameter(description = "페이지 번호 (1-base)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        TeacherApplicationListResult result = userQueryUsecase.getApplicationList(page, size);
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

    @GetMapping("/admin/users/instructor-applications/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "강사 신청자 상세 조회 (MS-4)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 신청자 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<TeacherApplicationDetailResponse>> getApplicationDetail(
            @Parameter(description = "신청자 user PK", example = "7")
            @PathVariable Long userId
    ) {
        TeacherApplication application = userQueryUsecase.getApplicationDetail(userId);
        TeacherApplicationDetailResponse response = toDetailResponse(application);
        return ResponseEntity.ok(ApiResponse.success(
                TeacherResponseCode.APPLICATION_DETAIL_FETCHED,
                TeacherResponseMessage.APPLICATION_DETAIL_FETCHED,
                response
        ));
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "강사 승인/반려 (MS-5)",
            description = "action=APPROVE 시 강사 승인, action=REJECT 시 반려. REJECT 는 reason 최소 10자.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 승인 또는 반려 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (action 누락, reason 10자 미만 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<TeacherActionResponse>> changeRole(
            @Parameter(description = "신청자 user PK", example = "7")
            @PathVariable Long userId,
            @Valid @RequestBody TeacherActionRequest request
    ) {
        TeacherActionResult result = "APPROVE".equals(request.action())
                ? userCommandUsecase.approve(new ApproveTeacherCommand(userId))
                : userCommandUsecase.reject(new RejectTeacherCommand(userId, request.reason()));

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
