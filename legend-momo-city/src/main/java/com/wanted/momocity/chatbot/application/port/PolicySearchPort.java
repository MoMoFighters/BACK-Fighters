package com.wanted.momocity.chatbot.application.port;

import java.util.List;

/* comment
    chatbot 서비스가 정책 검색 이라는 행위를 구현체와 상관없이 쓸 수 있게 해주는 애플리케이션 포트 인터페이스
 */

public interface PolicySearchPort {

    List<String> search(String query);

}
