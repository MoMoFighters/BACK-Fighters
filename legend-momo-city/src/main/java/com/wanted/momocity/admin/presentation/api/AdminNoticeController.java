package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.AdminNoticeCommandUseCase;
import com.wanted.momocity.admin.application.usecase.AdminNoticeQueryUseCase;
import com.wanted.momocity.admin.presentation.api.request.CreateNoticeRequest;
import com.wanted.momocity.admin.presentation.api.request.DeleteNoticesRequest;
import com.wanted.momocity.admin.presentation.api.request.UpdateNoticeRequest;
import com.wanted.momocity.admin.presentation.api.response.AdminNoticeDetailResponse;
import com.wanted.momocity.admin.presentation.api.response.AdminNoticeListResponse;
import com.wanted.momocity.admin.presentation.api.response.AdminNoticePageResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/admin-notices")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin - 공지", description = "관리자 공지 CRUD")
public class AdminNoticeController {

    private final AdminNoticeCommandUseCase commandUseCase;
    private final AdminNoticeQueryUseCase queryUseCase;

    // MS-11 공지 작성 — 요청 body를 Command로 변환 후 저장
    @PostMapping
    @Operation(summary = "공지 작성", description = "관리자가 공지를 작성한다.")
    public ResponseEntity<ApiResponse<Void>> createNotice(@RequestBody @Valid CreateNoticeRequest request) {
        commandUseCase.createNotice(request.toCommand());
        // POST 방식에서 리소스가 만들어지면 200 상태 코드보다는 생성 201 상태 코드가 맞다고 판단
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created((ApiResponseCode.CREATED) , "공지가 등록되었습니다!", null));
    }

    // MS-12 공지 목록 조회 — isPinned 필터 + 페이징
    // FE 컨벤션(1-based page)에 맞춰 page-1 변환 후 처리하고, 응답은 items 래퍼로 감싸 FE 스펙과 일치시킴
    @GetMapping
    @Operation(summary = "공지 목록 조회", description = "isPinned 파라미터가 없으면 전체 조회, 있으면 필터 조회한다.")
    public ResponseEntity<ApiResponse<AdminNoticePageResponse>> getNoticeList(
            @RequestParam(required = false) Boolean isPinned,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // FE는 page=1을 첫 페이지로 인식 → 내부 0-based 로 변환
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<AdminNoticeListResponse> result = queryUseCase.getNoticeList(isPinned, pageable)
                .map(AdminNoticeListResponse::from);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지 목록 조회 성공", AdminNoticePageResponse.from(result)));
    }

    // MS-16 공지 상세 조회
    @GetMapping("/{id}")
    @Operation(summary = "공지 상세 조회", description = "공지 id로 단건 상세 정보를 조회한다.")
    public ResponseEntity<ApiResponse<AdminNoticeDetailResponse>> getNoticeDetail(@PathVariable Long id) {
        AdminNoticeDetailResponse response = AdminNoticeDetailResponse.from(queryUseCase.getNoticeDetail(id));
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지 상세 조회 성공", response));
    }

    // MS-17 공지 수정
    @PutMapping("/{id}")
    @Operation(summary = "공지 수정", description = "공지 title과 content를 수정한다. isPinned는 수정 불가.")
    public ResponseEntity<ApiResponse<Void>> updateNotice(@PathVariable Long id,
                                                          @RequestBody @Valid UpdateNoticeRequest request) {
        commandUseCase.updateNotice(id, request.title(), request.content());
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 수정되었습니다.", null));
    }

    // MS-18 공지 단건 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "공지 단건 삭제", description = "공지 id로 단건을 삭제한다.")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        commandUseCase.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 삭제되었습니다.", null));
    }

    // MS-19 공지 선택 삭제 — body로 id 목록을 받아 한 번에 삭제
    @DeleteMapping
    @Operation(summary = "공지 선택 삭제", description = "id 목록으로 여러 공지를 한 번에 삭제한다.")
    public ResponseEntity<ApiResponse<Void>> deleteNotices(@RequestBody @Valid DeleteNoticesRequest request) {
        commandUseCase.deleteNotices(request.ids());
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 삭제되었습니다.", null));
    }

    // MS-21 공지 고정
    @PatchMapping("/{id}/pin")
    @Operation(summary = "공지 고정", description = "공지를 상단에 고정한다. 기존 고정 공지는 자동 해제된다.")
    public ResponseEntity<ApiResponse<Void>> pinNotice(@PathVariable Long id) {
        commandUseCase.pinNotice(id);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 상단에 고정되었습니다.", null));
    }

    // MS-22 공지 고정 해제
    @PatchMapping("/{id}/unpin")
    @Operation(summary = "공지 고정 해제", description = "고정된 공지의 고정을 해제한다.")
    public ResponseEntity<ApiResponse<Void>> unpinNotice(@PathVariable Long id) {
        commandUseCase.unpinNotice(id);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지 고정이 해제되었습니다.", null));
    }
}
