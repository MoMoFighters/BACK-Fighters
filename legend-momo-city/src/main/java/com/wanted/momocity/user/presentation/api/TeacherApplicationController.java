package com.wanted.momocity.user.presentation.api;

import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import com.wanted.momocity.user.application.command.ApproveTeacherCommand;
import com.wanted.momocity.user.application.command.RejectTeacherCommand;
import com.wanted.momocity.user.application.command.TeacherApplyCommand;
import com.wanted.momocity.user.presentation.api.request.TeacherApplyRequest;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase;
import com.wanted.momocity.user.domain.model.TeacherApplication;
import com.wanted.momocity.user.application.usecase.UserQueryUsecase.TeacherApplicationListResult;
import com.wanted.momocity.user.presentation.api.request.TeacherApproveRequest;
import com.wanted.momocity.user.presentation.api.request.TeacherRejectRequest;
import com.wanted.momocity.user.presentation.api.response.TeacherResponseCode;
import com.wanted.momocity.user.presentation.api.response.TeacherResponseMessage;
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


    @PostMapping("/teacherApply")
    @PreAuthorize("hasRole('STUDENT')") // 강사 신청은 학생만 가능
    @Operation(
            summary = "강사 신청",
            description = "강사는 해당 api를 통해 강사 증빙자료를 제출하고 강사 신청을 진행한다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 신청 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값 (@Valid 실패)"),
    })
    public ResponseEntity<ApiResponse<Void>> teacherApply(
            @Valid @ModelAttribute TeacherApplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        userCommandUsecase.teacherApply(
                new TeacherApplyCommand(userDetails.getUserId(),request.currentNickname(), request.nickname(), request.category(), request.proof())
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        TeacherResponseCode.SUCCESS,
                        TeacherResponseMessage.TEACHER_APPLIED,
                        null
                ));
    }

    @PatchMapping("/application-giveup")
    @Operation(description = "Rejected+student = 강사 신청 했다가 반려된 사람이 더이상 강사 신청을 하지 않고 " +
            "Active+student 가 되도록 ")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 포기 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> teacherGiveup(
            @AuthenticationPrincipal CustomUserDetails userDetails){

        userCommandUsecase.teacherGiveup(userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        TeacherResponseCode.SUCCESS,
                        TeacherResponseMessage.TEACHER_GIVEUP,
                        null
                ));
    }

    @GetMapping("/teacher-applications")
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

    @GetMapping("/teacher-application-detail/{userId}")
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


    @PatchMapping("/application-approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "강사 승인",
            description = "APPROVE 로 강사 승인")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 승인 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> teacherApprove(
            @Valid @RequestBody TeacherApproveRequest request
    ){
        userCommandUsecase.approve(new ApproveTeacherCommand(request.userId()));

        return ResponseEntity.ok(ApiResponse.success(
                TeacherResponseCode.APPROVED,
                TeacherResponseMessage.APPROVED,
                null
        ));
    }


    @PatchMapping("/application-reject/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "강사 반려",
            description = " REJECT 로 반려. reason 최소 10자.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "강사 반려 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신청자를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> teacherReject(
            @Parameter(description = "신청자 user PK", example = "7")
            @PathVariable Long userId,
            @Valid @RequestBody TeacherRejectRequest request
    ){
        userCommandUsecase.reject(new RejectTeacherCommand(userId, request.reason()));

        return ResponseEntity.ok(ApiResponse.success(
                TeacherResponseCode.REJECTED,
                TeacherResponseMessage.REJECTED,
                null
        ));
    }

    private TeacherApplicationListResponse.Item toItem(TeacherApplication app) {
        return new TeacherApplicationListResponse.Item(
                app.userId(),
                app.nickname(),
                app.name(),
                app.email(),
                app.category(),
                app.status(),
                app.role(),
                app.suspensionCount(),
                app.suspendedUntil(),
                app.createdAt()
        );
    }

    private TeacherApplicationDetailResponse toDetailResponse(TeacherApplication app) {
        return new TeacherApplicationDetailResponse(
                app.userId(),
                app.nickname(),
                app.name(),
                app.email(),
                app.profileImageUrl(),
                app.category(),
                app.proof(),
                app.status(),
                app.role(),
                app.createdAt(),
                app.fileType()
        );
    }
}
