package com.wanted.momocity.admin.infrastructure.adapter;


import com.wanted.momocity.admin.application.port.SystemHealthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/* comment.
    SystemHealthPort 구현체 : 인프라 4개의 상태를 실제로 체크해서 반환
    admin BC 가 선언하고 admin BC 가 직접 구현하는 어댑터
    < 이렇게 한 이유? >
    BC 간 통신 때문이 아니라, 같은 BC 안에서도 application 계층이
    infrastructure 계층을 직접 의존하지 않도록 계층 경계를 지키기 위해서
 */

// DB 체크용 — SELECT 1 쿼리로 연결 확인
// 메일 체크용 — createMimeMessage() 로 메일 서버 연결 확인
@Component
@RequiredArgsConstructor
public class SystemHealthAdapter implements SystemHealthPort {

    private final JdbcTemplate jdbcTemplate;
    private final JavaMailSender mailSender;

    // 4개 인프라 상태를 체크해서 HealthStatus 로 묶어 반환
    @Override
    public HealthStatus checkAll() {
        return new HealthStatus(
                checkWebService(),
                checkDatabase(),
                "정상",     // 파일 저장소 — S3 연동 전 임시 정상 처리
                checkMail()
        );
    }

    private String checkWebService() {
        return "정상";  // 이 메서드가 호출된다는 것 자체가 웹서비스 살아있다는 증명
    }

    private String checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "정상";
        } catch (Exception e) {
            return "비정상";
        }
    }

    private String checkMail() {
        try {
            mailSender.createMimeMessage();
            return "정상";
        } catch (Exception e) {
            return "비정상";
        }
    }
}