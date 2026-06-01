package com.wanted.momocity.admin.application.usecase;

import com.wanted.momocity.admin.domain.audit.ErrorLog;

import java.util.List;

/* comment.
    ErrorLogQueryUseCase 정리
    1. 이 인터페이스의 역할 : 에러 로그 조회 응용 계층 계약 / 컨트롤러 가 의존할 인터페이스.
    -> 실제 구현은 service 에서 구현
    2. 위치 : admin/application/usecase (응용 계층 - 계약)
    3. 왜 Query 전용인가 : Error 로그 조회는 데이터 변경 X (저장 시스템이 자동으로, admin 은 조회만 한다.
    4. 왜 Result record 를 중첩으로 두는가 : UseCase 가 무엇을 반환하는지 같은 파일을 두면 한눈에 파악할 수 있기 때문이다.
    5. 왜 도메인 객체(ErrorLog) 를 그대로 노출하는가 (강사 영역 패턴과 동일) : Controller 가 도메인 -> DTO 변환 책임 가짐
    -> UseCase 가 변환까지 안 함.
 */
public interface ErrorLogQueryUseCase {

    ErrorLogList getRecent(int limit);

    /* comment.
        ErrorLogList 정리
        1. 이 record 의 역할 : Error 로그 목록 데이터 묶음. UseCase 의 출력 계약
        2. 왜 List 만 있는데 굳이 record 로 감싸는가 : 지금은 List 하나밖에 없지만, 향후 여러가지 기능이 추가될 수 있다.
        3. 향후 확장 여지 (예: totalCount, hasMore 등 추가 시) : 전체 Error 로그 수 / 다음 페이지 있는지 / 가장 오래된 에러 시각
     */
    record ErrorLogList(
            List<ErrorLog> errorLogs
    ) { }
}