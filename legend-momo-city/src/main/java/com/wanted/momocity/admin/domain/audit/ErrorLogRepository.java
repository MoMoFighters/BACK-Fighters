package com.wanted.momocity.admin.domain.audit;

import java.util.List;

/* comment.
    ErrorLogRepository 정리
    1. 이 인터페이스의 역할 :
    2. 위치 : admin/domain/audit (도메인 계층)
    3. 왜 도메인 계층에 Repository 인터페이스를 두는가 (DDD) :
        - 도메인이 "필요한 약속" 정의
        - 인프라(JPA Adapter)가 그 약속을 구현 (DIP)
        - 도메인은 인프라 모름 → 테스트/교체 쉬움
    4. 메서드 3개 의도 :
        - save(errorLog)            : 신규 에러 발생 시 저장
        - findRecent(limit)         : 최근 N개 조회 (대시보드 위젯용)
        - findByLevel(level, limit) : 심각도별 필터 (예: CRITICAL 만)
    5. 왜 페이지네이션(Pageable) 안 쓰는가 :
        - FE 에러 로그 위젯은 작은 영역 - 최근 N개만 표시
        - 추후 전체 페이지 필요해지면 Pageable 추가 가능
 */
public interface ErrorLogRepository {

    /**
     * 신규 에러 로그 저장
     */
    ErrorLog save(ErrorLog errorLog);

    /**
     * 최근 에러 로그 N개 조회 (occurredAt 내림차순)
     */
    List<ErrorLog> findRecent(int limit);

    /**
     * 특정 심각도의 최근 에러 로그 N개 조회
     */
    List<ErrorLog> findByLevel(ErrorLevel level, int limit);
}