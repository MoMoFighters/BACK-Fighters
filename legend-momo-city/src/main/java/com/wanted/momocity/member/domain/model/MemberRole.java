package com.wanted.momocity.member.domain.model;


/* comment.
    회원 역할 : user.role 컬럼 값과 매핑.
    ---
    왜 ENUM 을 사용했는가?
    1. 타입 안전성 : MemberRole.TEACHER 라고 오타를 작성하게 되면 컴파일 에러로 발견할 수 있지만,
    문자열을 런타임에 가서야 발견되기 때문이다.
    2. 자기 문서화 : MemberRole 보면 가능한 값이 3개로 한정됨을 즉시 인지할 수 있음.
    3. 확장 가능성 : ENUM 타입 특성상 메서드를 붙일 수 있음. (EX : isAdmin())
 */

/* comment.
    MemberRole 열거형 클래스 정리
    1. 이 ENUM 은 어떤걸 뜻하나? : 회원의 역할 / 권한을 분류하기 위해서
    2. 왜 ENUM 을 사용했는가? : 문자열 대신 타입 안정성을 위해서
    3. 데이터베이스의 어떤 컬럼과 맵핑되는가? : user.role
    4. 3개 값의 의미 : student(학생)/teacher(강사)/admin(관리자)
    5. 사용 위치 : Member.role, MemberJpaEntity.role 변환, 강사 영역의 MemberUserAdapter 에서 필터링
 */

public enum MemberRole {
    STUDENT,
    TEACHER,
    ADMIN
}
