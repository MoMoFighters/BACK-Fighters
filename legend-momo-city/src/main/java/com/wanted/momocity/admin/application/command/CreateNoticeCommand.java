package com.wanted.momocity.admin.application.command;

/* comment.
    CreateNoticeCommand 클래스
    공지 작성 요청에 필요한 데이터를 application 레이어로 전달하는 데이터 묶음.
    presentation 에 받은 HTTP 요청을 도메인 언어로 변환하는 역할
 */

public record CreateNoticeCommand(
        String title,
        String content,
        boolean isPinned
) {}
