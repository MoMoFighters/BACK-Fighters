package com.wanted.momocity.friend.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.friend.application.usecase.FindUserQueryUseCase.FindView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 검색 결과 응답 객체")
@JsonInclude(JsonInclude.Include.NON_NULL) //null일 때 json 미출력
public record FindUserResponse(
        @Schema(description = "유저 ID", example = "3")Long userId,
        String nickname,
        String name,
        String status,
        String role,
        String lectureTitle,
        String profileImageUrl
) {
    //서비스에서 받은 날 것의 FindView 주머니를 여기서 가공
    public static FindUserResponse from(FindView view) {
        //비활성 유저 닉네임 가공
        String displayNickname = view.nickname();
        if (view.isNotActive()) {
            displayNickname += "(알 수 없음)";
        }

        //강의명 가공
        String finalLectureTitle = null;
        List<String> lectureTitleList = view.lectureTitle();
        if (lectureTitleList != null && !lectureTitleList.isEmpty()) {
            finalLectureTitle = "(" + String.join(", ", lectureTitleList) + ")";
        }

        return new FindUserResponse(
                view.userId(),
                "TEACHER".equals(view.role()) ? view.name() : null, //강사일 때 이름
                displayNickname,
                view.status(),
                view.role(),
                finalLectureTitle,
                view.profileImageUrl()
        );
    }
}
