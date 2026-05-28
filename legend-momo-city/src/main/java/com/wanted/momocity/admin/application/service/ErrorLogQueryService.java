package com.wanted.momocity.admin.application.service;

import com.wanted.momocity.admin.application.usecase.ErrorLogQueryUseCase;
import com.wanted.momocity.admin.domain.audit.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* comment.
    ErrorLogQueryService 정리
    1. 이 클래스의 역할 : ErrorLogQueryUseCase 의 실 구현체. Repository 에 조회 위임 + UseCase 의 출력 형식으로 변환
    2. 위치 : admin/application/service (응용 계층 - 구현)
    3. 왜 @Transactional(readOnly = true) 인가 : Query 라서 쓰기 작업이 존재하지 않는다.
    4. 왜 ErrorLogRepository 에 직접 의존 (PORT 패턴 안 씀) : ErrorLog 는 admin 자체 도메인이다.
    -> 외부 BC 가 아니라 BC 경계 격리 불필요 -> 도메인 Repository 인터페이스 직접 의존 가능하다.
    5. AdminDashboardQueryService 와의 핵심 차이 (의존 방식) : 의존성의 종류가 다르다. AdminDashboard 는 외부 BC
    의존이기 때문에 PORT 방식, ErrorLog 는 자체 도메인이라 Repository 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErrorLogQueryService implements ErrorLogQueryUseCase {

    private final ErrorLogRepository errorLogRepository;

    @Override
    public ErrorLogList getRecent(int limit) {
        return new ErrorLogList(errorLogRepository.findRecent(limit));
    }
}
