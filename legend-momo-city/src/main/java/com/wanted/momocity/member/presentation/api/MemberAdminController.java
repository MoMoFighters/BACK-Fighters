package com.wanted.momocity.member.presentation.api;

import com.wanted.momocity.member.application.usecase.MemberCommandUseCase;
import com.wanted.momocity.member.presentation.api.request.ChangeMemberStatusRequest;
import com.wanted.momocity.member.presentation.api.response.ChangeMemberStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/* comment.
    MemberAdminController 정리
    1. 이 클래스의 역할 : 관리자 영역의 회원 상태 변경 HTTP API 진입점
    2. 다루는 API :
        - PATCH /api/v1/admin/users/{userId}/status
    3. 클래스 레벨 어노테이션 :
        - @RestController : 클래스가 REST API 컨트롤러임을 표시
        - @RequestMapping("/api/v1/admin") : 클래스 안 모든 핸들러 URL 앞에
        - @PreAuthorize("hasRole('ADMIN')") : 모든 핸들러 호출 전 권한 검사
        - @Tag : Swagger UI 에서 Member - 회원 상태 관리로 표시
    4. 의존성 :
        - MemberCommandUseCase : UseCase 인터페이스 의존
        - 왜 구현체(Service) 가 아니라 인터페이스 의존? : 구현체 교체 가능하게 하기 위해서
    5. Controller 의 책임 5단계 :
        a) HTTP 요청 받기
        b) Request DTO -> Command 변환
        c) UseCase 호출
        d) Result -> Response 변환
        e) ResponseEntity 로 응답 반환
    6. Controller 가 *하지 않는 일* :
        - 비즈니스 로직 (UseCase/Service)
        - DB 직접 접근 (Repository/Adapter)
        - 도메인 검증 (Member 도메인 모델)
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Member - 회원 상태 관리", description = "관리자가 회원 상태 변경 (MS-6)")
public class MemberAdminController {

    private final MemberCommandUseCase memberCommandUseCase;

    public MemberAdminController(MemberCommandUseCase memberCommandUseCase) {
        this.memberCommandUseCase = memberCommandUseCase;
    }

    /* comment.
        실제 구현 시 흐름 (m03 우선순위 3) :
        1. MemberStatus newStatus = MemberStatus.valueOf(request.status())   // String -> enum
        2. ChangeMemberStatusCommand command = new ChangeMemberStatusCommand(userId, newStatus)
        3. MemberStatusChangeResult result = memberCommandUseCase.changeStatus(command)
        4. ChangeMemberStatusResponse response = new ChangeMemberStatusResponse(
               result.userId(), result.status(), result.changedAt())
        5. return ResponseEntity.ok(response)
     */
    @PatchMapping("/users/{userId}/status")
    @Operation(
            summary = "회원 상태 변경 (MS-6)",
            description = "관리자가 회원 상태를 ACTIVE / BANNED / BLACK / DELETED 등으로 변경한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 status 값 또는 userId 형식 오류 (validation 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 만료"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    public ResponseEntity<ChangeMemberStatusResponse> changeStatus(
            @Parameter(description = "회원 user PK", example = "7")
            @PathVariable Long userId,
            @Valid @RequestBody ChangeMemberStatusRequest request
    ) {
        throw new UnsupportedOperationException("TODO: m03 우선순위 3 - MS-6 회원 상태 변경 컨트롤러 구현");
    }
}