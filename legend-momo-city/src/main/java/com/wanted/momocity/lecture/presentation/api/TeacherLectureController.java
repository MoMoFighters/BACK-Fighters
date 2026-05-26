package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.presentation.api.request.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.response.CreateLectureResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/lectures")
public class TeacherLectureController {

    private final LectureCommandUseCase lectureCommandUseCase;

    public TeacherLectureController(LectureCommandUseCase lectureCommandUseCase) {
        this.lectureCommandUseCase = lectureCommandUseCase;
    }

    @PostMapping
    // API를 호출할 수 있는 권한을 제한하는 어노테이션
    // 즉 강사만 이 API를 호출 할 수 있다.
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<CreateLectureResponse>> createLecture(
            Authentication authentication,
            @Valid @RequestBody CreateLectureRequest request
    ) {
        // JwtTokenProvider가 Authentication name에 로그인 사용자의 email을 넣어둔다.
        String teacherEmail = authentication.getName();

        Lecture lecture = lectureCommandUseCase.createLecture(request.toCommand(teacherEmail));

        // 201 Created와 강의 등록 성공 메시지를 반환한다.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "강의가 등록되었습니다.",
                        CreateLectureResponse.from(lecture)
                ));
    }
}