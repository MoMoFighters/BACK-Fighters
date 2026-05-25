package com.wanted.momocity.lecture.presentation.api.request;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.application.command.CreateLectureCommand;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import jakarta.validation.constraints.NotBlank;

/*
 * CreateLectureRequest는 프론트에서 전달하는 강의 등록 요청 DTO
 * category는 enum으로 바로 받지 않고 String으로 받은 뒤 직접 변환
 * 그래야 허용되지 않은 카테고리 값이 들어왔을 때 500이 아니라 400 응답으로 처리
 */
public record CreateLectureRequest(
        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @NotBlank(message = "강의 설명은 필수입니다.")
        String description,

        String thumbnailUrl,

        @NotBlank(message = "강의 카테고리는 필수입니다.")
        String category
) {

    /*
     * presentation 요청 DTO를 application command로 변환한다.
     *
     * 이 과정에서 category 문자열을 LectureCategory enum으로 변환한다.
     */
    public CreateLectureCommand toCommand(Long teacherId) {
        return new CreateLectureCommand(
                teacherId,
                title,
                description,
                thumbnailUrl,
                parseCategory(category)
        );
    }

    /*
     * 요청으로 들어온 category 문자열을 LectureCategory enum으로 변환한다.
     *
     * 허용되지 않은 값이면 도메인 규칙 예외를 던져 400 응답으로 처리되게 한다.
     */
    private LectureCategory parseCategory(String category) {
        try {
            return LectureCategory.valueOf(category);
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleViolationException("허용되지 않은 강의 카테고리입니다.");
        }
    }
}