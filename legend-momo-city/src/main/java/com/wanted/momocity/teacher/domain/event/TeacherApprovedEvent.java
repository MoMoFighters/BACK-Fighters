package com.wanted.momocity.teacher.domain.event;

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

// record + implements 사용한 이유
    // record 도 인터페이스 구현 가능하다.
    // 단 부모 클래스는 상속이 불가능하다
        // 왜냐하면 record 가 이미 Record 클래스를 상속 받기 때문이다.)
public record TeacherApprovedEvent(
        Long userId, // 누가 승인 되었나?
        Instant occurredAt // 언제 승인되었나?
) implements DomainEvent {
}
