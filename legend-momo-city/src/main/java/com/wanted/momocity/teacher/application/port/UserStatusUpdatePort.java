package com.wanted.momocity.teacher.application.port;

/* comment.
    userStatusUpdatePort 정리
    1. 해당 클래스가 하는 일 : 강사 영역이 회원영역에 강사 승인/반려를 요청하는 약속
    2. 위치 : teacher/application/port
    3. UserQueryPort 와 짝꿍 (CQRS) :
        - UserQueryPort       : 조회 (Query) 약속
        - UserStatusUpdatePort : 변경 (Command) 약속
        - 같은 영역(회원) 을 향하지만 *책임 분리*
    4. 호출 흐름 :
        강사 영역 CommandService
        → UserStatusUpdatePort (이 인터페이스) → MemberUserAdapter
        → MemberQueryService.findById (회원 조회)
        → Member.approveAsTeacher() / rejectAsTeacher() (도메인 행위)
        → MemberRepository.save (저장)
    5. 메소드 반환이 void 인 이유 : Command 는 결과 없이 성공/실패만. 실패 시 예외
    6. 도메인 검증은 어디서? : 회원 영역의 Member 도메인이 책임. 강사 영역은 요청만 진행하게 된다.
 */

public interface UserStatusUpdatePort {

    void approveTeacher(Long userId);

    void rejectTeacher(Long userId, String reason);
}
