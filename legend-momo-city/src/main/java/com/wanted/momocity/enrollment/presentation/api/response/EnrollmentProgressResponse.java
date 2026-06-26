package com.wanted.momocity.enrollment.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
// null 필드 JSON에서 제외
// category가 없으면 progressByCategory, building 관련 필드는 빠진다.
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
        // 카테고리 O, 해당 카테고리 건물 url
        String buildingUrl
) {

}
