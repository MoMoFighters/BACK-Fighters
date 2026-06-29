package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.port.UserNamePort;
import com.wanted.momocity.admin.application.usecase.AccessLogQueryUseCase;
import com.wanted.momocity.admin.domain.access.AccessLog;
import com.wanted.momocity.admin.domain.access.AccessLogAction;
import com.wanted.momocity.admin.domain.access.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/* comment.
    AccessLogQueryUseCase 구현체.
    page + limit -> Pageable 로 변환해서 Repository 에 위임한다.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// implements 를 해야만 UseCase 의 계약을 이행하는 클래스가 된다.
public class AccessLogQueryService implements AccessLogQueryUseCase {

    private final AccessLogRepository accessLogRepository;
    // userId → 이름/역할 일괄 조회 포트 (N+1 방지)
    private final UserNamePort userNamePort;

    // 비로그인 포함 + 전체 접근 로그를 최신순으로 페이지 조회
    @Override
    public AccessLogResult getAll(int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<AccessLog> logPage = accessLogRepository.findAll(pageable);
        return new AccessLogResult(logPage, resolveUserInfo(logPage));
    }

    // 비로그인 포함 + action 으로 필터링해서 최신순으로 페이지 조회
    @Override
    public AccessLogResult getByAction(AccessLogAction action, int page, int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<AccessLog> logPage = accessLogRepository.findByAction(action, pageable);
        return new AccessLogResult(logPage, resolveUserInfo(logPage));
    }

    // 페이지 내 비익명 userId 를 모아 한 번에 이름/역할 조회
    private Map<Long, UserNamePort.UserInfo> resolveUserInfo(Page<AccessLog> logPage) {
        Set<Long> userIds = logPage.getContent().stream()
                .map(AccessLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return userNamePort.getUserInfoByUserIds(userIds);
    }

}
