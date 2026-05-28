package com.wanted.cleanarchitecture.learning.presentation.api;

import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponse;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseCode;
import com.wanted.cleanarchitecture.global.presentation.api.common.ApiResponseMessage;
import com.wanted.cleanarchitecture.learning.application.command.CompleteModuleCommand;
import com.wanted.cleanarchitecture.learning.application.usecase.LearningCommandUseCase;
import com.wanted.cleanarchitecture.learning.application.usecase.LearningQueryUseCase;
import com.wanted.cleanarchitecture.learning.presentation.api.request.CompleteModuleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning")
@Tag(name = "Learning", description = "학습 진행 상태 조회와 모듈 완료 처리를 담당하는 Learning Context API")
public class LearningController {

    private final LearningCommandUseCase learningCommandUseCase;
    private final LearningQueryUseCase learningQueryUseCase;

    public LearningController(
            LearningCommandUseCase learningCommandUseCase,
            LearningQueryUseCase learningQueryUseCase
    ) {
        this.learningCommandUseCase = learningCommandUseCase;
        this.learningQueryUseCase = learningQueryUseCase;
    }

    @GetMapping("/progresses/{progressId}")
    @Operation(
            summary = "학습 진행 단건 조회",
            description = "학습 진행 ID로 사용자의 모듈 학습 상태를 조회한다."
    )
    public ResponseEntity<ApiResponse<LearningQueryUseCase.LearningProgressView>> getProgress(
            @Parameter(description = "조회할 학습 진행 ID", example = "1")
            @PathVariable Long progressId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                learningQueryUseCase.handle(progressId)
        ));
    }

    @PutMapping("/module-completions/{moduleId}")
    @Operation(
            summary = "모듈 완료 처리",
            description = "사용자가 특정 모듈을 완료 처리한다. 활성 수강 여부는 LearningAccessPolicy에서 검증한다."
    )
    public ResponseEntity<ApiResponse<Void>> completeModule(
            @Parameter(description = "완료 처리할 모듈 ID", example = "1")
            @PathVariable Long moduleId,
            @Valid @RequestBody CompleteModuleRequest request
    ) {
        learningCommandUseCase.handle(new CompleteModuleCommand(request.userId(), request.courseId(), moduleId));
        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.MODULE_COMPLETED,
                ApiResponseMessage.MODULE_COMPLETED
        ));
    }
}
