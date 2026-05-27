package com.wanted.momocity.member.domain.model;

/* comment.
    MemberCategory 열거형 클래스 정리
    1. 해당 ENUM 은 무엇을 하는가? : 회원의 관심사 카테고리
    2. 데이터베이스 매핑 : user.category
    3. 5개 값의 의믜
    HEALTH(헬스/운동) / STUDY(공부/자기개발) / COOK(요리)
    BEAUTY(뷰티) / ART(예술/창작)
    4. 강제성 : 회원의 관심사 분류이기 때문에 NULL 허용 가능 ( ERD 기준 - nullable ).MemberRole/MemberStatus 처럼 필수 X
    5. 사용 위치 : Member.category, MemberJpaEntity.category 변환, 강사 신청자 목록/상세 응답 category 필드
    6. 영역 경계 처리 : 강사 영역의 TeacherApplication 은 이 ENUM 을 직접 챙기지 않고 STRING 타입으로 받는다.
 */

public enum MemberCategory {
    HEALTH,
    STUDY,
    COOK,
    BEAUTY,
    ART
}
