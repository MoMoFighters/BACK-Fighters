package com.wanted.momocity.user.domain.event;

import com.wanted.momocity.global.domain.common.event.DomainEvent;

import java.time.Instant;


/* comment.
    TeacherApprovedEvent 정리
    1. 해당 클래스가 하는 일 : 강사 신청이 승인되었다! 라는 이미 발생한 도메인 사실
    2. 위치 : teacher/domain/event (이벤트 폴더!)
    3. command 와 차이점은?
        a) ApproveTeacherCommand : 강사 승인해 주세요! {앞으로 수행할 요청}
        b) TeacherApprovedEvent : 강사가 승인되었어요! {이미 발생한 결과값}
    4. 발행자와 수신자는?
        {발행자}
            - TeacherApplicationCommandService 가 승인 처리 후 발행
            - <만약 Member.approveAsTeacher() 도메인 메소드 내부에서 발행을 하게 된다면...?>
        {수신자}
            - 알림 영역(notification 바운더리 컨텍스트) -> 강사에게 승인 알람 발송 (module04 예정)
            - module03 에서는 수신부만 일단락 존재
 */

/* comment.
    TeacherRejectedEvent 정리
    1. 해당 클래스가 하는 일 : 강사 신청이 반려되었다 라는 {이미 알려주는 도메인}
    2 위치 : teacher/domain/event {event!}
    3. Approved 와 차이점은?
        - reason(반려 사유) 필드 1개 추가
    4. 발행자 & 수신자
        {발행자}
        - module03 에서는 구현부에서 TeacherApplicationCommandService.reject() 처리 후 발행
        {수신자}
        - 알림 영역 - 강사에게 반려 사유 포함한 알림 발송 예정 {module04 진행 예정}
        - module03 에서는 수신부만 진행
    5. 왜 reason 을 Event 에 직접 넣었는가?
        - 알림 발송 시 사유 가 핵심 정보
        - 수신자(알림 영역) 가 reason 별도 조회하려면 -> DB 를 한 번 더 호출하게 된다.{ 비효율적이다. }
        - 후속 처리에 바로 필요한 정보는 Event 에 넣자가 우리가 정한 원칙
    6. 하지만 Event 가 뚱뚱해지면 곤란한 이유 { 이벤트에는 최소 필수 정보만 넣자! }
        - 강사 이름, 이메일 등은 수신자가 별도 조회. event 에 넣지 않음
        - reason 은 반려 행위의 본질적 일부라 exception
 */

/*comment
*  어차피 승인이든 거절이든 보내는 이메일 포멧은 동일하여
* TeacherRejectedEvent와 TeacherApprovedEvent를 TeacherApplicationEvent로 통합한다 */

// record + implements 사용한 이유
// record 도 인터페이스 구현 가능하다.
// 단 부모 클래스는 상속이 불가능하다
// 왜냐하면 record 가 이미 Record 클래스를 상속 받기 때문이다.)

public record TeacherApplicationEvent(
        Long userId,
        String reason,
        Instant occurredAt
) implements DomainEvent {
}
