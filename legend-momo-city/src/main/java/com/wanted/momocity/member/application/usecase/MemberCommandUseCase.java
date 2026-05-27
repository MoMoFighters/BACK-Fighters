package com.wanted.momocity.member.application.usecase;

import com.wanted.momocity.member.application.command.ChangeMemberStatusCommand;

import java.time.LocalDateTime;

/* comment.
    MemberCommandUseCase 정리
    1. 이 인터페이스의 역할 : 회원 상태 변경의 응용 계층 계약 정의
    2. 위치 : member/application/usecase (응용 계층 - 계약)
    3. 왜 interface 인가 : 구현 교체 또는 테스트를 진행할 때 mock 을 사용하기 위함
    4. 왜 Result 를 인터페이스 안에 중첩 record 로 두는가 : 응집도를 위해
 */
public interface MemberCommandUseCase {

    MemberStatusChangeResult changeStatus(ChangeMemberStatusCommand command);

    /* comment.
        MemberStatusChangeResult 정리
        1. 이 record 의 역할 : 상태 변경 작업 결과 데이터에 묶는 용도
        2. 왜 use case 와 같은 파일에 두는가 : useCase 출력 계약도 useCase 인터페이스 안에 있다. 응집도를 위해서
        3. 어떤 필드가 들어가는가 :
            - userId : 변경된 회원 Id
            - status : 변경 후 상태
            - changedAt : 변경 시각
     */
    record MemberStatusChangeResult(
            Long userId,
            String status,
            LocalDateTime changedAt
    ) { }
}