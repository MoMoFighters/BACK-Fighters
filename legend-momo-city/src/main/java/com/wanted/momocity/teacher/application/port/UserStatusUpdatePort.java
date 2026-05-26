package com.wanted.momocity.teacher.application.port;

/*
 * 강사 영역이 회원 영역에 "회원 상태 변경"을 요청하기 위한 인터페이스.
 *
 * 구현체: teacher/infrastructure/member/MemberUserAdapter
 *
 * 책임 분리:
 *  - 강사 영역의 응용 서비스는 user 테이블이나 Member 도메인 모델을 직접 조작하지 않는다.
 *  - 실제 상태 전이 검증(PENDING→ACTIVE / PENDING→REJECTED)과 저장은
 *    회원 영역의 Member 도메인 + MemberRepository 가 담당한다.
 */
public interface UserStatusUpdatePort {

    void approveTeacher(Long userId);

    void rejectTeacher(Long userId, String reason);
}
