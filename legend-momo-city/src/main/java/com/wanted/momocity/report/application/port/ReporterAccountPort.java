// ============================================================================
// [DEACTIVATED / 전체 주석처리 — 2026-06-01 신고 접수 리팩토링]
// 사유: ReportController 가 인증 principal(CustomUserDetails.getUserId())에서
//       신고자 userId 를 직접 얻도록 변경됨 → email→userId 변환용 이 port 가 불필요.
//       (FriendController / LectureController 와 동일한 principal 사용 컨벤션으로 통일)
// 복구: 아래 주석 해제 + ReportController / ReportCommandService 를 원복.
// ============================================================================
//
// package com.wanted.momocity.report.application.port;
//
// /* comment.
//     ReporterAccountPort 정리
//     1. 역할 : 신고자의 email 을 받아서 내부 PK 로 변환해주는
//     2. 위치 : 응용 계층 - 외부 BC 접근 인터페이스
//     3. WHY Port 패턴 사용
//        → 신고 도메인은 회원 BC 를 직접 import 하면 BC 경계가 깨지기 때문이다.
//        → Port 인터페이스만 응용 계층에만 두고, 실제 회원 조회는 인프라 계층의 Adapter 가 담당하게 된다.
//        → 신고 도메인 코어는 회원 BC 의 존재를 모른다.
//     4. WHY 단일 메서드만 (getReporterId)
//        → 신고가 회원에 대해 알아야할 정보는 userId 밖에 없다.
//        → 회원의 다른 정보에 대해서는 신고 도메인이 알아야할 필요가 없다.
//     5. enrollment 의 StudentAccountPort 와 차이
//        → StudentAccountPort : 학생 권한만 허용 (수강신청은 학생만 가능)
//        → ReporterAccountPort : 모든 회원이 신고 가능하다. role 체크가 없다.
//  */
// public interface ReporterAccountPort {
//
//     Long getReporterId(String email);
// }
