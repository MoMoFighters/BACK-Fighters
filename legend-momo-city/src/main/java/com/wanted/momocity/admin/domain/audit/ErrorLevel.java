package com.wanted.momocity.admin.domain.audit;

/* comment.
    ErrorLevel 정리
    1. 이 enum 의 역할 :
    2. 위치 : admin/domain/audit (도메인 계층 - 감사/모니터링)
    3. 값 3개 의미 :
        - CRITICAL : 서비스 중단 수준 (즉시 대응 필요)
        - ERROR    : 기능 실패 (사용자 영향 있음)
        - WARNING  : 경고 (사용자 영향 없음, 모니터링 필요)
    4. FE 매핑 :
        - 대시보드 에러 로그 위젯에서 색상 뱃지로 표시
        - CRITICAL=빨강 / ERROR=주황 / WARNING=노랑
    5. 왜 DEBUG/INFO 안 포함 :
        - 정상 흐름 로그는 에러 로그 위젯 대상 아님
        - AOP 의 log.debug / log.info 와 별개 (그건 흐름 추적)
 */
public enum ErrorLevel {
    CRITICAL,
    ERROR,
    WARNING
}