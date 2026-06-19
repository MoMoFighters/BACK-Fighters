package com.wanted.momocity.enrollment.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
// null 필드 JSON에서 제외
@JsonInclude(JsonInclude.Include.NON_NULL)
// 강의 진척도 API 응답
public record EnrollmentProgressResponse(

        // 카테고리 X 전체 진척도
        Integer myTotalProgress,
        // 카테고리 O, 해당 카테고리 진척도
        Integer progressByCategory,
        // 건물 레벨
        Integer buildingLevel,
        // 건물 현재 경험치
        Integer buildingCurrentExp,
        // 건물 필요 경험치
        Integer buildingTotalExp,
        // 이어서 학습할 강의 Id
        Long lectureId,
        // 이어서 학습할 강의 제목
        String lectureTitle,
        // 이어서 학습할 챕터 Id
        Long chapterId,
        // 이어서 학습할 챕터 제목
        String chapterTitle,
        // 이어서 학습할 챕터의 진행률
        Integer chapterProgress
) {

}
