package com.wanted.momocity.admin.application.port;

/* comment.
    사용자 ID -> 이름 + 역할 변환 포트 - user BC 수영님께서 어댑터로 구현
    신고자 이름, 접근 로그 이름/역할 조회에 공통으로 사용
 */

import java.util.Map;
import java.util.Set;

public interface UserNamePort {

    // 여러 ID를 한 번에 받아 Map으로 반환 — 낱건 조회 시 쿼리 N번 발생 방지
    Map<Long, UserInfo> getUserInfoByUserIds(Set<Long> userIds);

    record UserInfo(
            String name, String role
    ) {}

}