package com.wanted.momocity.admin.application.usecase;

/* comment.
    접근 로그 조회 응용 계층 계약
    getAll : 전체 조회 (action 필터 없음)
    getByAction : action 기준 필터 조회
 */

import com.wanted.momocity.admin.application.port.UserNamePort;
import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface AccessLogQueryUseCase {

    // 로그 페이지 + userId → 이름/역할 Map 을 함께 반환하는 래퍼
    record AccessLogResult(Page<AccessLog> page, Map<Long, UserNamePort.UserInfo> userInfoMap) {}

    // action 필터 없이 전체 접근 로그를 page/limit 기준으로 페이지 조회
    AccessLogResult getAll(int page, int limit);

    // action(LOGIN, LOGOUT, FORBIDDEN) 기준으로 필터링해서 페이지 조회
    AccessLogResult getByAction(AccessLogAction action, int page, int limit);

}
