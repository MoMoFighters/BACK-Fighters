package com.wanted.momocity.admin.presentation.api;

import com.wanted.momocity.admin.application.usecase.AdminNoticeCommandUseCase;
import com.wanted.momocity.admin.application.usecase.AdminNoticeQueryUseCase;
import com.wanted.momocity.admin.presentation.api.request.CreateNoticeRequest;
import com.wanted.momocity.admin.presentation.api.request.DeleteNoticesRequest;
import com.wanted.momocity.admin.presentation.api.request.UpdateNoticeRequest;
import com.wanted.momocity.admin.presentation.api.response.AdminNoticeDetailResponse;
import com.wanted.momocity.admin.presentation.api.response.AdminNoticeListResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<ApiResponse<Void>> createNotice(@RequestBody @Valid CreateNoticeRequest request) {
        commandUseCase.createNotice(request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 등록되었습니다.", null));
    }

    // MS-12 공지 목록 조회 — isPinned 필터 + 페이징
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminNoticeListResponse>>> getNoticeList(
            @RequestParam(defaultValue = "false") boolean isPinned,
            Pageable pageable) {
        Page<AdminNoticeListResponse> result = queryUseCase.getNoticeList(isPinned, pageable)
                .map(AdminNoticeListResponse::from);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지 목록 조회 성공", result));
    }

    // MS-16 공지 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminNoticeDetailResponse>> getNoticeDetail(@PathVariable Long id) {
        AdminNoticeDetailResponse response = AdminNoticeDetailResponse.from(queryUseCase.getNoticeDetail(id));
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지 상세 조회 성공", response));
    }

    // MS-17 공지 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(@PathVariable Long id,
                                                          @RequestBody @Valid UpdateNoticeRequest request) {
        commandUseCase.updateNotice(id, request.title(), request.content());
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 수정되었습니다.", null));
    }

    // MS-18 공지 단건 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        commandUseCase.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 삭제되었습니다.", null));
    }

    // MS-19 공지 선택 삭제 — body로 id 목록을 받아 한 번에 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteNotices(@RequestBody @Valid DeleteNoticesRequest request) {
        commandUseCase.deleteNotices(request.ids());
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "공지가 삭제되었습니다.", null));
    }

}
