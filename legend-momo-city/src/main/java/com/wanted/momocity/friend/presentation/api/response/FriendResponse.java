package com.wanted.momocity.friend.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.FriendView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 친구 목록 결과 응답 객체")
@JsonInclude(JsonInclude.Include.NON_NULL) //null일 때 json 출력 안함
public record FriendResponse(
        Long userId,
        String name, //강사 이름
        String nickname,
        String role,
        String status, //친구 여부
        String lectureTitle, //백엔드가 가공해서 보낼 강의명,
        String profileImageUrl
) {

    //데이터 가공
    //from: A라는 객체로부터(from) 데이터를 받아서 나(B)를 만든다.
    public static FriendResponse from(FriendView view) {
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

        return new FriendResponse(
                view.userId(),
                "TEACHER".equals(view.role()) ? view.name() : null,
                displayNickname,
                view.role(),
                view.status(),
                finalLectureTitle,
                view.profileImageUrl()
        );
    }
}
