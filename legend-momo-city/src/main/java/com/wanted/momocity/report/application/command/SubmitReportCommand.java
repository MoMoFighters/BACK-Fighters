package com.wanted.momocity.report.application.command;

import com.wanted.momocity.report.domain.model.ReportReason;
import com.wanted.momocity.report.domain.model.ReportTargetType;

/* comment.
    SubmitReportCommand 정리
    1. 역할 : 신고 접수 UseCase 의 입력 묶음. Controller 가 HTTP 요청을 받아서 이걸로 변환해 Usecase 에 넘겨준다.
    2. 위치 : 응용 계층 - 입력 객체
    3. WHY record 사용
       → 불변하는 객체이기 때문
    4. WHY Request DTO 가 아니라 Command 라는 별도 객체
       → 표현 계층 = 응용 계층 분리
       → Command : 응용 계층
       → 두 계층을 분리하면 HTTP 구조 바뀌어도 Command 는 안바뀐다.
    5. 필드 5개 의미
       - reporterUserId : 신고자의 userId (인증 principal 에서 추출)
       - targetType : 신고 대상 종류
       - targetId : 신고 대상 ID
       - reason : 신고 사유 ENUM
       - detail : 자유 설명
 */
public record SubmitReportCommand(
        Long reporterUserId,
        ReportTargetType targetType,
        Long targetId,
        ReportReason reason,
        String detail
) {
}