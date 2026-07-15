package com.wanted.momocity.chatbot.application.port;

/* comment.
    Chatbot BC 가 review 도메인의 수강평 원문을 필요로 할 때, review 내부 구조를 직접
    알지 않고 이 포트를 통해서만 접근 가능한 경계
 */

import java.util.List;

public interface ReviewInfoPort {

    List<String> getReviewContents(Long lectureId);

}
