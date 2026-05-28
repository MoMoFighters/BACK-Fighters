package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.query.GetLecturesQuery;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.presentation.api.response.LecturePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * LectureController는 일반 사용자가 보는 강의 조회 API를 담당합니다.
 *
 * 강사용 강의 관리는 TeacherLectureController에서 처리하고,
 * 여기서는 메인/마이페이지 강의 목록 조회를 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@Tag(name = "Lecture", description = "학생용 강의 목록 조회 API")
public class LectureController {

    // 강의 조회 UseCase입니다.
    private final LectureQueryUseCase lectureQueryUseCase;

    /**
     * 강의 목록 조회 API
     *
     * 예:
     * GET /api/v1/lectures?enrolled=true
     * GET /api/v1/lectures?category=STUDY&enrolled=false&page=0&size=10
     */
    @Operation(
            summary = "강의 목록 및 수강 내역 조회",
            description = "학생용 강의 목록을 조회합니다. enrolled=true이면 내가 수강신청한 강의만 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<LecturePageResponse>> getLectures(
            Authentication authentication,

            // 강의 카테고리 필터
            // 없으면 전체 카테고리 조회
            @RequestParam(required = false) String category,

            // 수강 신청 여부 필터
            // true: 신청한 강의만
            // false: 신청하지 않은 강의만
            // null: 수강 여부 상관없이 전체 조회
            @RequestParam(required = false) Boolean enrolled,

            // 페이지 번호입니다. 기본값은 0
            @RequestParam(defaultValue = "1") int page,

            // 페이지 크기입니다. 기본값은 10
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = Long.parseLong(authentication.getName());

        GetLecturesQuery query = new GetLecturesQuery(
                userId,
                parseCategory(category),
                enrolled,
                page,
                size
        );

        LecturePageResponse response = lectureQueryUseCase.getLectures(query);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의 목록 조회에 성공했습니다.",
                response
        ));
    }

    /*
     * 문자열 category를 LectureCategory enum으로 변환
     * category가 없으면 null을 반환해서 전체 카테고리를 조회
     */
    private LectureCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        try {
            return LectureCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않는 강의 카테고리입니다.");
        }
    }
}
