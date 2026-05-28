package com.wanted.cleanarchitecture.catalog.presentation.api;

import com.wanted.cleanarchitecture.catalog.application.command.CreateCourseCommand;
import com.wanted.cleanarchitecture.catalog.application.usecase.CourseCommandUseCase;
import com.wanted.cleanarchitecture.catalog.application.usecase.CourseQueryUseCase;
import com.wanted.cleanarchitecture.catalog.presentation.api.request.CreateCourseRequest;
import com.wanted.cleanarchitecture.catalog.presentation.api.response.CreateCourseResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseCode;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    // command(dml)
    private final CourseCommandUseCase courseCommandUseCase;
    // query(read)
    private final CourseQueryUseCase courseQueryUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCourseResponse>> createCourse(@RequestBody CreateCourseRequest request) {

        Long courseId = courseCommandUseCase.handle(new CreateCourseCommand(
                request.authorId(),
                request.title(),
                request.description()
        ));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.COURSE_CREATED,
                        ApiResponseMessage.COURSE_CREATED,
                        new CreateCourseResponse(courseId)
                ));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseQueryUseCase.CourseView>> getCourse(@PathVariable Long courseId) {

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                courseQueryUseCase.handle(courseId)
        ));
    }

}
