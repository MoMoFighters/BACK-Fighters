package com.wanted.momocity.user.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* comment.
    TeacherApplication 정리
    1. 해당 클래스가 하는 역할 : 강사 역할의 조회용 도메인 값 객체
    2. record 를 사용한 이유 : 모든 필드에는 자동적으로 final 을 선언하게 된다.
        - 이를 통해서 불변성을 유지할 수 있다. (생성 후 변경 불가능)
    3. 9개 필드 의미 :
        - userId   : 강사 신청서 식별자(PK)
        - email    : 신청자 이메일
        - name     : 본명
        - nickname : 닉네임
        - birth    : 생년월일
        - profileImageUrl : 프로필 사진 URL
        - category : 관심 카테고리 (HEALTH/STUDY/COOK/BEAUTY/ART 중 하나, String 으로 받음)
        - proof    : 증빙 서류 URL
        - appliedAt : 신청일 (ERD 에 컬럼 없어 updated_at 빌린다.)
            - 추후에 컬럼을 추가할지 지금처럼 진행할지 팀원들과 협의
 */

public record TeacherApplication(
        Long userId,
        String email,
        String name,
        String nickname,
        LocalDate birth,
        String profileImageUrl,
        // category 가 String 인 이유는?
        // - 회원 영역 MemberCategory enum 타입을 직접 받을 수 있지만 String 선택
        // - 이유 : 영역 경계는 enum 타입으로 결합 시키지 않음
        // - 영역 내부에는 enum 타입으로, 영역 경게 통과 시 String 으로 약화
        String category,
        String proof,
        LocalDateTime appliedAt
) {
    // 검증 후 자동 대입
    // - 컴파일러가 자동으로 this.userId = userId 등 대입 코드 생성
    // - 일반 생성자처럼 손으로 적지 않아도 된다는 장점을 보유!
    public TeacherApplication {
        if (userId == null) {
            throw new DomainRuleViolationException("강사 신청서 식별자(userId)는 필수입니다.");
        }
    }
}
