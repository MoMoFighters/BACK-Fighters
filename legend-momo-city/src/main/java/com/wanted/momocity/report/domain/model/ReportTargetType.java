package com.wanted.momocity.report.domain.model;

/* comment.
    ReportTargetType 정리
    1. 이 enum 의 역할 : 신고 대상의 종류를 분류하는 enum (게시글/댓글/회원/강의)
    2. 위치 : report/domain/model (도메인 계층)
    3. 4 값 의미 :
        - POST     : 게시글 신고 (module04 - 커뮤니티)
        - COMMENT  : 댓글 신고 (module04 - 커뮤니티)

        - USER     : 회원 신고 (module03 사용 가능)
        - LECTURE  : 강의 신고 (module03 사용 가능)
    4. module03 에서 사용 가능한 값 : USER, Lecture (커뮤니티는 module04 에서 구현이라 실사용 X)
    5. POST/COMMENT 미리 선언하는 이유 : ReportStatus 와 동일한 패턴이다.
 */
public enum ReportTargetType {
    POST,
    COMMENT,
    USER,
    LECTURE
}