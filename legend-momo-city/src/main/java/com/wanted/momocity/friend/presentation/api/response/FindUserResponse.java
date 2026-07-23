package com.wanted.momocity.friend.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.friend.application.usecase.FriendQueryUseCase.FindView;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 검색 결과 응답 객체")
@JsonInclude(JsonInclude.Include.NON_NULL) //null일 때 json 미출력
public record FindUserResponse(
        @Schema(description = "유저 ID", example = "3")Long userId,
        String name,
        String nickname,
        String status,
        String role,
        String lectureTitle,
        String profileImageUrl
) {
    //서비스에서 받은 날 것의 FindView 주머니를 여기서 가공
    public static FindUserResponse from(FindView view) {
        //user 담당자가 ACTIVE가 아닌 건 알 수 없음 가공 처리하기 때문에 ACTIVE이면서 친구가 아닌 경우에 알 수 없음 가공

        return new FindUserResponse(
                view.userId(),
                "TEACHER".equals(view.role()) ? view.name() : null, //강사일 때 이름
                view.nickname(),
                view.status(),
                view.role(),
                view.lectureTitle() != null && !view.lectureTitle().isEmpty()
                ? String.join(", ", view.lectureTitle()) : null,
                view.profileImageUrl()
        );
    }
}
