package com.wanted.momocity.report.application.port;

// CHAT 타입 신고의 채팅 내용 조회 — chat BC 의 ChatContentAdapter 가 구현
public interface ChatContentPort {
    String getContentById(Long chatId);
}
