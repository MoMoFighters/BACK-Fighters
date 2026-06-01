package com.wanted.momocity.enrollment.infrastructure.adapter;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.Role;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.enrollment.application.port.StudentAccountPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthStudentAccountAdapter implements StudentAccountPort {

    private final LoadUserPort loadUserPort;

    public AuthStudentAccountAdapter(LoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    @Override
    public Long getStudentId(Long userId) {
        // 인증 과정에서 전달받은 email로 사용자 정보를 조회한다.
        User user = loadUserPort.findById(userId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("인증된 사용자 정보를 찾을 수 없습니다."));

        // 조회된 사용자가 학생(STUDENT) 권한이 아니면 수강신청을 허용하지 않는다.
        if (user.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("학생 회원만 수강신청할 수 있습니다.");
        }
        // 수강신청 도메인에서는 email이 아니라 내부 PK인 user.id를 학생 식별자로 사용한다.
        return user.getId();
    }
}
