package com.wanted.momocity.report.domain.model;

/* comment.
    ReportReason 정리
    1. 이 enum 의 역할 : 신고 사유를 분류하는 enum. 통계/필터링 + FE 의 신고 모달 옵션 매핑
    2. 위치 : report/domain/model (도메인 계층)
    3. 5 값 의미 :
        - SPAM           : 스팸/광고
        - ABUSE          : 욕설/혐오 표현
        - INAPPROPRIATE  : 부적절한 내용 (성적, 폭력 등)
        - COPYRIGHT      : 저작권 침해
        - OTHER          : 기타 (자유 설명은 Report.detail 필드로)
    4. 확장 여지 : 향후 사기, 혐오발언, 기타 등으로 설명이 가능하다.
 */
public enum ReportReason {
    SPAM,
    ABUSE,
    INAPPROPRIATE,
    COPYRIGHT,
    OTHER
}