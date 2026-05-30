package com.wanted.momocity.user.application.service;

import com.wanted.momocity.auth.application.port.PasswordEncodePort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.user.application.command.ApproveTeacherCommand;
import com.wanted.momocity.user.application.command.NicknameRegisterCommand;
import com.wanted.momocity.user.application.command.RejectTeacherCommand;
import com.wanted.momocity.user.application.command.UpdateUserInfoCommand;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.port.UserEmailSendPort;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.domain.exception.InvalidReasonException;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserCommandService implements UserCommandUsecase {

    private final UserRepository userRepository;
    private final UserPolicy userPolicy;
    private final PasswordEncodePort passwordEncodePort;
    private final UserEmailSendPort userEmailSendPort;

    @Override
    public String registerNickname(NicknameRegisterCommand command) {
        userPolicy.nicknamePolicy(command.nickname());
        userRepository.registerNickname(command.userId(), command.nickname());
        log.info("[user] 닉네임 등록 완료 | userId={} | nickname={}", command.userId(), command.nickname());
        return command.nickname();
    }

    @Override
    public void updateUserInfo(UpdateUserInfoCommand command) {
        // 닉네임 있으면 중복 확인
        if (command.nickname() != null) {
            userPolicy.nicknamePolicy(command.nickname());
        }

        String encodedPassword = null;
        if (command.password() != null) {
            String storedPassword = userRepository.findPasswordById(command.userId());
            userPolicy.passwordPolicy(command.currentPassword(), command.password(), storedPassword);
            encodedPassword = passwordEncodePort.encode(command.password());  // 검증 통과 후 암호화
            userRepository.clearTempPwd(command.userId());
            log.info("[user] 비밀번호 변경 완료 | userId={}", command.userId());
        }

        userRepository.updateUserInfo(new UpdateUserInfoCommand(
                command.userId(),
                command.profileImageUrl(),
                command.nickname(),
                null,//현재 비번은 저장 안 함
                encodedPassword
        ));
    }

    // 강사승인
    @Override
    public TeacherActionResult approve(ApproveTeacherCommand command) {

        String email = userRepository.findById(command.userId())
                .orElseThrow(() -> new DomainRuleViolationException("사용자를 찾을 수 없습니다."))
                .getEmail();

        userRepository.updateRoleAndStatus(command.userId(), Role.TEACHER, Status.ACTIVE);
        userEmailSendPort.sendTeacherResult(email, "ACTIVE", null);
        log.info("[teacher] 강사 승인 처리 | userId={}", command.userId());
        return new TeacherActionResult(command.userId(), "ACTIVE", null, LocalDateTime.now());
    }

    // 강사거절
    @Override
    public TeacherActionResult reject(RejectTeacherCommand command) {

        if (command.reason() == null || command.reason().length() < 10) {
            throw new InvalidReasonException("반려 사유는 최소 10자 이상이어야 합니다.");
        }

        String email = userRepository.findById(command.userId())
                .orElseThrow(() -> new DomainRuleViolationException("사용자를 찾을 수 없습니다."))
                .getEmail();

        userRepository.updateRoleAndStatus(command.userId(), Role.TEACHER, Status.REJECTED);
        userEmailSendPort.sendTeacherResult(email, "REJECTED", command.reason());
        log.info("[teacher] 강사 반려 처리 | userId={} | reason={}", command.userId(), command.reason());
        return new TeacherActionResult(command.userId(), "REJECTED", command.reason(), LocalDateTime.now());
    }
}