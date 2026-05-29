package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.query.*;
import com.wanted.momocity.lecture.application.usecase.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.presentation.api.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/* comment
 * LectureController는 일반 사용자가 보는 강의 조회 API를 담당
 *
 * 강사용 강의 관리는 TeacherLectureController에서 처리하고,
 * 여기서는 메인/마이페이지 강의 목록 조회를 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lectures")
@Tag(name = "Lecture", description = "학생용 강의 목록 조회 API")
public class LectureController {

    // 강의 조회 UseCase
    private final LectureQueryUseCase lectureQueryUseCase;
    // 관리자 강의 조회 UseCase
    private final AdminLectureQueryUseCase adminLectureQueryUseCase;

    // 강의 목록 조회 API
    @Operation(
            summary = "강의 목록 및 수강 내역 조회",
            description = "학생용 강의 목록을 조회합니다. enrolled=true이면 내가 수강신청한 강의만 조회합니다."
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

        // 관리자 강의 목록 조회
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

        // 강사 강의 목록 조회
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

        /*
         * 학생 강의 목록 조회
         * 학생은 ACTIVE 상태의 강의만 조회하고,
         * 응답에는 수강 여부(isEnrolled)가 포함된다.
         */
        GetLecturesQuery query = new GetLecturesQuery(
                userId,
                parseCategory(category),
                enrolled,
                keyword,
                page,
                size
        );

        StudentLecturePageResponse response =
                lectureQueryUseCase.getLectures(query);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "강의 목록 조회에 성공했습니다.",
                response
        ));
    }

    // Authentication에 들어있는 ROLE 정보를 꺼낸다.
    // 예: ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN
    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElseThrow(() -> new DomainRuleViolationException("사용자 권한 정보가 없습니다."));
    }

    // 학생 강의 상세 조회 API
    // 학생은 ACTIVE 상태의 강의만 상세 조회
    @Operation(
            summary = "강의 상세 조회",
            description = "권한에 따라 강의 상세 정보를 조회합니다. 관리자는 승인 대기/진행 중 강의를 조회하고, 학생은 ACTIVE 강의만 조회합니다."
    )
    @GetMapping("/{lectureId}")
    public ResponseEntity<ApiResponse<?>> getLectureDetail(
            Authentication authentication,

            // 상세 조회할 강의 ID
            @PathVariable Long lectureId
    ) {
        /*
         * Authorization 토큰에서 로그인 사용자 ID와 권한을 꺼낸다.
         * 같은 URL이라도 권한에 따라 조회 정책과 응답 DTO가 달라진다.
         */
        Long userId = Long.parseLong(authentication.getName());
        String role = getRole(authentication);

        /*
         * 관리자 강의 상세 조회
         * 관리자는 승인 대기(WAITING) 또는 진행 중(ACTIVE) 강의를 상세 조회한다.
         */
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

        /*
         * 학생 강의 상세 조회
         * 학생은 ACTIVE 상태의 강의만 상세 조회한다.
         * 응답에는 isEnrolled가 포함된다.
         */
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

    // 문자열 status를 LectureStatus enum으로 변환한다.
    // status가 없으면 null을 반환해서 관리자 전체 조회로 처리한다.
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

    /*
     * 문자열 category를 LectureCategory enum으로 변환한다.
     * category가 없으면 null을 반환해서 전체 카테고리 조회로 처리한다.
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
