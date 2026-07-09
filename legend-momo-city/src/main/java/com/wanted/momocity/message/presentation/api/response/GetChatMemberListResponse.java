package com.wanted.momocity.message.presentation.api.response;

import com.wanted.momocity.message.application.usecase.MessageQueryUseCase;

import java.util.List;

public record GetChatMemberListResponse(
        List<MemberInfo> memberInfo
) {
    public record MemberInfo(
            Long userId,
            String name,
            String nickname,
            String role,
            String status, //친구 상태
            String profileImageUrl
    ) {}

    //로그인 유저 닉네임 뒤에 "(나)" 붙이도록 가공
    public static GetChatMemberListResponse from(List<MessageQueryUseCase.ChatMemberView> views, Long loginUserId) {

        List<MemberInfo> memberResponseList = views.stream()
                .map(member -> {
                    boolean isMe = member.userId().equals(loginUserId);

                    //로그인 유저 본인이면 닉네임 뒤에 "(나)" 접미사 붙이기
                    String displayNickname = isMe
                            ? member.nickname() + "(나)"
                            : member.nickname();

                    return new MemberInfo(
                            member.userId(),
                            member.name(),
                            displayNickname,
                            member.role(),
                            member.status(),
                            member.profileImageUrl()
                    );
                }).toList();

        return new GetChatMemberListResponse(memberResponseList);
    }
}
