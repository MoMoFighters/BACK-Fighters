package com.wanted.momocity.member.domain.model;

import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* comment.
    Member 도메인 모델 정리
    1. 이 클래스의 역할 : 회원 영역의 회원이라는 비즈니스 개념을 표현하는 자바 객체이다.
    2. user 테이블 1행 : Member 1행 (1:1 매핑)
    3. 다른 영역은 이 클래스를 직접 만들지 않음. MemberRepository 또는 MemberQueryService 를 거쳐서만 접근
    4. JPA 어노테이션 X : 도메인은 DB 를 모른다.
 */

public class Member {

    /* comment.
        - 회원이라는 비즈니스 개념을 표현하는 필드 선언
        - 불변 그리고 가변 필드 구분으로 도메인 규칙을 타입 시스템 차원에서 강제성을 부여
            - private : 캡슐화 / private 없을 경우 가변 가능
        - 왜 일부만 final 인 것인가? 정체성은 불변하고, 상태 전이 필드만 가변으로 하면 실용적이기 때문
     */

    private final Long id;
    private final String email;
    private final String name;
    private final String nickname;
    private final LocalDate birth;
    private final String profileImageUrl;
    private MemberRole role;
    private MemberStatus status;
    private final MemberCategory category;
    private final String proof;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Member(Long id, String email, String name, String nickname, LocalDate birth,
                   String profileImageUrl, MemberRole role, MemberStatus status,
                   MemberCategory category, String proof,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {

        /* comment.
            항상 유효한 객체 보장. 잘못된 데이터로는 애초에 생성이 불가능하게 사전 통제
            private 으로 외부 호출으로 부터 보호
            - 왜 검증을 생성자에서 하는가?
            -> DDD의 항상 유효 불변식 패턴을 사용
         */

        if (email == null || email.isBlank()) {
            throw new DomainRuleViolationException("이메일은 필수입니다.");
        }
        if (role == null) {
            throw new DomainRuleViolationException("회원 역할은 필수입니다.");
        }
        if (status == null) {
            throw new DomainRuleViolationException("회원 상태는 필수입니다.");
        }
        this.id = id;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.status = status;
        this.category = category;
        this.proof = proof;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /* comment.
        DB 에서 읽어온 값으로 기존 객체를 복원하는 진입점
        각각의 이름이 의도를 뜻함
     */
    public static Member restore(Long id, String email, String name, String nickname, LocalDate birth,
                                 String profileImageUrl, MemberRole role, MemberStatus status,
                                 MemberCategory category, String proof,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Member(id, email, name, nickname, birth, profileImageUrl,
                role, status, category, proof, createdAt, updatedAt);
    }

    /* comment.
        회원의 비즈니스 행위정의
        도메인 규칙 검증 및 상태 변경을 이 메소드 안에서 책임
     */
    // PENDING -> ACTIVE 전이 검증 + 상태 변경 (role=TEACHER 유지)
    public void approveAsTeacher() {
        throw new UnsupportedOperationException("TODO: 강사 승인 도메인 행위 (m03 우선순위 1)");
    }
    // PENDING -> REJECTED 전이 검증 + 상태 변경 (로그인 차단)
    public void rejectAsTeacher() {
        throw new UnsupportedOperationException("TODO: 강사 반려 도메인 행위 (m03 우선순위 1)");
    }

    // 관리자가 수동으로 상태 변경. 어떤 상태로든 전이 가능
    public void changeStatusByAdmin(MemberStatus newStatus) {
        throw new UnsupportedOperationException("TODO: 회원 상태 변경 도메인 행위 (m03 우선순위 3)");
    }

    /* comment.
        외부에서 읽기만 허용한다.
        쓰기는 비즈니스 메서드를 통해서만 쓸 수 있다.
        - @Getter 어노테이션을 쓰지 않은 이유
        1. 도메인 모델은 순수 자바를 유지하는게 명시성에 좋다고 판단
        2. 강사 예제 Course 도 직접 작성
        3. 단, JpaEntity는 BaseTimeEntity 를 상속하기 위해 @Getter 어노테이션 사용
     */

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getNickname() { return nickname; }
    public LocalDate getBirth() { return birth; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public MemberRole getRole() { return role; }
    public MemberStatus getStatus() { return status; }
    public MemberCategory getCategory() { return category; }
    public String getProof() { return proof; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
