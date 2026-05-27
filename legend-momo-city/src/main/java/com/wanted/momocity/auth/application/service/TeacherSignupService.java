package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.TeacherSignupCommand;
import com.wanted.momocity.auth.application.policy.SignupPolicy;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.usecase.TeacherSignupUseCase;
import com.wanted.momocity.auth.domain.event.SignupCompletedEvent;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeacherSignupService implements TeacherSignupUseCase {

    private final UserRepository userRepository;
    private final SignupPolicy signupPolicy;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodePort emailCodePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void signup(TeacherSignupCommand command) {

        // 정책 확인
        signupPolicy.ensureEligible(command.email());

        // 이메일(id), 비밀번호, 이름, 카테고리, 증빙자료 url 넘겨서 새로운 강사 자바 객체 생성
        User user = userRepository.register(User.teacherRegister(command.email(), passwordEncoder.encode(command.password()), command.name(), command.category(),command.proof()));

        // 이메일 인증 하고서 인증됨 의 상태를 지움
        emailCodePort.deleteVerified(command.email());

        // 회원가입 하고서 이벤트 발행 - 나와의 채팅 생성용
        eventPublisher.publishEvent(new SignupCompletedEvent(user.getId()));
    }
}
