package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.presentation.api.request.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.response.CreateLectureResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teacher/lectures")
public class TeacherLectureController {

    private final LectureCommandUseCase lectureCommandUseCase;
    private final S3UploadPort s3UploadPort;
    public TeacherLectureController(
            LectureCommandUseCase lectureCommandUseCase,
            S3UploadPort s3UploadPort
    ) {
        this.lectureCommandUseCase = lectureCommandUseCase;
        this.s3UploadPort = s3UploadPort;
    }

    // Content-Type: multipart/form-data 즉, Json이 아닌 Form-data로 요청 받는다
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // 실행 전 권한 검증
    @PreAuthorize("hasAuthority('ROLE_TEACHER')") // 강사일 때만 이 API 가 호출 된다.
    public ResponseEntity<ApiResponse<CreateLectureResponse>> createLecture(
            Authentication authentication,
            @Valid @ModelAttribute CreateLectureRequest request
    ) {
        String teacherEmail = authentication.getName();

        /*
         * S3 업로드 전에 category를 먼저 검증
         * 잘못된 카테고리 요청이면 여기서 400 응답으로 끝나고, 썸네일 파일은 업로드 X
         */
        request.validateCategory();

        String thumbnailUrl = s3UploadPort.upload(request.thumbnail());

        Lecture lecture = lectureCommandUseCase.createLecture(
                request.toCommand(teacherEmail, thumbnailUrl)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "강의가 등록되었습니다.",
                        CreateLectureResponse.from(lecture)
                ));
    }
}