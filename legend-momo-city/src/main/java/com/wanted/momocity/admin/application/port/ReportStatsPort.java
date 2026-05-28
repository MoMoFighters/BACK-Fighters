package com.wanted.momocity.admin.application.port;

/* comment.
    ReportStatsPort 정리
    1. 역할 : admin BC 가 report BC 의 신고 통계를 가져오기 위한 외부 BC 접근 계약
    2. 위치 : admin/application/port (응용 계층 - 외부 BC 접근 인터페이스)
    3. WHY Port 패턴 사용 (직접 의존 X)
       → admin BC 는 report BC 의 내부 구현을 모름 (BC 격리)
       → MemberStatsPort / LectureStatsPort 와 동일 패턴 → BC 정합성
    4. WHY countAll 단일 메서드
       → 대시보드 위젯이 필요한 정보는 "전체 신고 수" 하나
       → 추가 통계 필요 시 메서드 확장 (예: countByStatus)
 */
public interface ReportStatsPort {

    /** 전체 신고 수 (대시보드 통계용) */
    long countAll();
}
