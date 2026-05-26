package com.wanted.momocity.lecture.presentation.api;

import com.wanted.momocity.global.presentation.api.common.ApiResponse;
import com.wanted.momocity.global.presentation.api.common.ApiResponseCode;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.presentation.api.request.CreateLectureRequest;
import com.wanted.momocity.lecture.presentation.api.response.CreateLectureResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/lectures")
public class TeacherLectureController {

    private final LectureCommandUseCase lectureCommandUseCase;

    public TeacherLectureController(LectureCommandUseCase lectureCommandUseCase) {
        this.lectureCommandUseCase = lectureCommandUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateLectureResponse>> createLecture(
            @RequestHeader("TeacherId") Long teacherId,
            @Valid @RequestBody CreateLectureRequest request
    ) {
        Lecture lecture = lectureCommandUseCase.createLecture(request.toCommand(teacherId));

        /*
         *  201 Created와 강의 등록 성공 메시지를 반환
         */
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        "강의가 등록되었습니다.",
                        CreateLectureResponse.from(lecture)
                ));
    }
}