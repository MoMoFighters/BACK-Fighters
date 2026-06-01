package com.wanted.momocity.lecture.infrastructure.adapter;

import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.domain.model.Role;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.lecture.application.port.TeacherAccountPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

/* comment
 * Auth와 TeacherAccountPort와 연결해주는 adapter
 * 이 adapter를 통해 인증된 사용자 정보를 조회
 */
@Component
public class AuthTeacherAccountAdapter implements TeacherAccountPort {

    private final LoadUserPort loadUserPort;

    public AuthTeacherAccountAdapter(LoadUserPort loadUserPort) {
        this.loadUserPort = loadUserPort;
    }

    /* comment
     * JWT에서 추출한 email로 사용자를 조회하고,
     * 해당 사용자가 강사인지 확인한 뒤 user id를 teacherId로 반환
     */
    @Override
    public Long getTeacherId(Long userId) {
        User user = loadUserPort.findById(userId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("인증된 사용자 정보를 찾을 수 없습니다."));

        if (user.getRole() != Role.TEACHER) {
            throw new AccessDeniedException("강사 회원만 강의를 등록할 수 있습니다.");
        }

        return user.getId();
    }
}