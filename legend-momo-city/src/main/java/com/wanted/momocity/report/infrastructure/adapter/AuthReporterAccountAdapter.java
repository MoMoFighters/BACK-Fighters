// ============================================================================
// [DEACTIVATED / 전체 주석처리 — 2026-06-01 신고 접수 리팩토링]
// 사유: ReportController 가 인증 principal(CustomUserDetails.getUserId())에서
//       신고자 userId 를 직접 얻도록 변경됨 → auth BC 로 건너가 email→userId 조회하던
//       이 어댑터가 불필요해짐. (회원은 이미 인증된 principal 에 userId 를 보유)
// 복구: 아래 주석 해제 + ReportController / ReportCommandService 를 원복.
// ============================================================================
//
// package com.wanted.momocity.report.infrastructure.adapter;
//
// import com.wanted.momocity.auth.application.port.LoadUserPort;
// import com.wanted.momocity.auth.domain.model.User;
// import com.wanted.momocity.report.application.port.ReporterAccountPort;
// import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
// import org.springframework.stereotype.Component;
//
// /* comment.
//     AuthReporterAccountAdapter 정리
//     1. 역할 : ReporterAccountPort 인터페이스 auth BC 의 LoadUserPort 로 구현하는 어댑터
//     2. 위치 : 인프라 계층 - 외부 BC 접근 어댑터
//     3. WHY @Component 사용
//        → Spring 이 빈으로 등록 -> Service 에 자동 주입
//        → DB가 아니라 외부 BC 호출이기 때문에 @Repository 어노테이션 대신 @Component 를 사용
//     4. WHY LoadUserPort 를 주입받음
//        → 신고 BC 는 회원 DB 에 직접 접근하면 BC 경계를 침범하게 된다.
//        → auth BC 가 제공하는 LoadUserPort 를 빌려 쓰면 BC 격리 유지
//        → email -> User 변환 책임 auth BC 에 위임
//     5. WHY role 체크 없음 (StudentAccountAdapter 와 차이)
//        → 수강신청은 학생만 가능
//        → 신고는 모든 회원이 가능
//     6. 의존 방향
//        - implements ReporterAccountPort : 인프라가 도메인 약속 지킴
//        - LoadUserPort 주입 : 다른 BC 의 응용 계층
//        - User 사용 : 인프라가 외부 BC 도메인
//  */
// @Component
// public class AuthReporterAccountAdapter implements ReporterAccountPort {
//
//     private final LoadUserPort loadUserPort;
//
//     public AuthReporterAccountAdapter(LoadUserPort loadUserPort) {
//         this.loadUserPort = loadUserPort;
//     }
//
//     @Override
//     public Long getReporterId(String email) {
//         User user = loadUserPort.findByEmail(email)
//                 .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
//                         "인증된 사용자 정보를 찾을 수 없습니다."
//                 ));
//         return user.getId();
//     }
// }
