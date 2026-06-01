package com.wanted.momocity.admin.presentation.api.response;

/* comment.
    DashboardSummaryResponse 정리
    1. 이 record 의 역할 : 대시보드 통계 결과(카운트 + 증감률)를 클라이언트에 돌려주는 HTTP 응답 body
    2. 위치 : admin/presentation/api/response (표현 계층 - 출력 DTO)
    3. 왜 응용 Result(DashboardSummary) 와 분리하는가 : 응용 출력과 HTTP 응답 격리
    4. 왜 필드가 응용 Result 와 동일한가 : 응용 출력 그대로 클라이언트에 전달
    5. 왜 import 가 하나도 없는가 : long / double 은 자바 기본형. 외부 클래스 의존성 0
    6. 증감률 표시 방법 :
        - memberGrowthRate / lectureGrowthRate 는 % (소수점 가능). 예) 12.5 → FE 에서 "+12.5%"
        - reportCount 는 증감률 없음 (FE 화면에 신고 카운트만 표시)
 */
public record DashboardSummaryResponse(
        long memberCount,
        double memberGrowthRate,
        long lectureCount,
        double lectureGrowthRate,
        long reportCount
) { }