package com.wanted.momocity.chatbot.domain.exception;

/* comment.
    momo-ai 서비스 호출이 실패했을 때 던질 예외
 */

public class PolicySearchException extends RuntimeException {
    public PolicySearchException(String message) {
        super(message);
    }
}
