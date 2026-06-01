package com.wanted.momocity.member.domain.model;

/* comment.
    MemberStatus 열거형 클래스 정리
    1. 해당 ENUM 은 무엇을 하는가? : 회원 계정의 상태
    2. 데이터베이스 매핑 : user.status 컬럼
    3. 6개 값의 의미
       ACTIVE(정상 활성) / PENDING(강사 신청 대기) / REJECTED(강사 신청 반려)
       BANNED(기간정지) / BLACK(영구 정지) / DELETED(회원 탈퇴)
    4. 폐지된 값 : 기존에는 SUSPENDED 가 있었으나, 인수인계서에서 패지 후 BLACK 과 BANNED 로 분배
    5. 상태 전이 흐름
        a) 강사 승인의 경우
        PENDING -> ACTIVE {강사 영역의 Member.approveAsTeacher()
        b) 강사 반려의 경우
        PENDING -> REJECTED {강사 영역의 Member.rejectAsTeacher()}
        c) 관리자가 수동으로 처리할 경우
        ACTIVE -> BANNED/BLACK/DELETED -> 회원 영역의 Member.changeStatusByAdmin()
     6. module03 미구현 항목 : BANNED/BLACK 값은 enum 에 있지만 자동 정지 로직은 module04 에서
     진행 예정. ERD 에 suspended_until, violation_count 같은 컬럼 존재 X
     module03 에서는 해당 enum 자리만 보존예정
     7. 사용 위치 : Member.status, MemberJpaEntity.status 변환, 강사 영역의 신청자 필터
     status='PENDING' 회원 상태 변경 API
 */

public enum MemberStatus {
    ACTIVE,
    PENDING,
    REJECTED,
    BANNED,
    BLACK,
    DELETED
}
