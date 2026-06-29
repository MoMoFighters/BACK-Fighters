package com.wanted.momocity.report.application.port;

import java.util.Map;
import java.util.Set;

/* comment.
    report BC 가 신고자/피신고자 이름이 필요할 때 사용하는 Port
    admin BC 의 UserNamePort 와 역할 동일하나 BC 경계상 별도 선언
    수영님이 user BC 쪽에서 어댑터를 구현한다.
 */
public interface ReportUserNamePort {

    // 여러 userId 를 한 번에 받아 Map 으로 반환 — 낱건 조회 시 N+1 방지
    Map<Long, String> getNamesByUserIds(Set<Long> userIds);
}