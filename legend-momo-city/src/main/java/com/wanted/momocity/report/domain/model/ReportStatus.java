package com.wanted.momocity.report.domain.model;

/* comment.
    ReportStatus 정리
    1. 이 enum 의 역할 : 신고의 라이프사이클 상태를 표현하는 enum
    2. 위치 : report/domain/model (도메인 계층)
    3. 4 값 의미 :
        - PENDING    : 신고 접수됨 (module03 의 신고 접수 결과)
        - REVIEWING  : 관리자가 검토 중 (module04)
        - CONFIRMED  : 신고 인정 (module04)
        - REJECTED   : 신고 기각 (module04)
    4. module03 에서 실질 사용은 PENDING 만 : 검토/인정/기각은 module04 의 검토 기능에서 처리 예정
    -> module03 에서는 PENDING 상태로 저장만 진행한다.
    5. 그래도 4 값 다 미리 선언하는 이유 : module04 에서 추가하면 enum 값이 변경되어 Report 도메인 시그니처/저장 데이터 영향이 있을 수 있기 때문
 */
public enum ReportStatus {
    PENDING,
    REVIEWING,
    CONFIRMED,
    REJECTED
}