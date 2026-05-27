package com.wanted.momocity.admin.presentation.api.response;

/* comment.
    DashboardSummaryResponse 정리
    1. 이 record 의 역할 : 대시보드 통계 결과를 클라이언트에 돌려주는 HTTP 응답 바디
    2. 위치 : admin/presentation/api/response (표현 계층 - 출력 DTO)
    3. 왜 응용 Result(DashboardSummary) 와 분리하는가 : 응용 출력과 HTTP 응답 격리
    4. 왜 필드가 응용 Result 와 동일한가 : 응용 출력 그대로 클라이언트에 전달
    5. 왜 import 가 하나도 없는가 : long 은 자바 기본형. 외부 클래스 의존성 0
 */
public record DashboardSummaryResponse(
        long memberCount,
        long reportCount,
        long lectureCount
) { }