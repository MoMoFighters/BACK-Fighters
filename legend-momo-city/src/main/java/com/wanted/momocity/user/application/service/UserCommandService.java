package com.wanted.momocity.user.application.service;

import com.wanted.momocity.auth.application.port.PasswordEncodePort;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.user.application.port.GetItemUrlPort;
import com.wanted.momocity.user.application.port.GoogleDriveUploadPort;
import com.wanted.momocity.user.domain.event.TeacherApplicationEvent;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.user.application.command.*;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.domain.exception.InvalidReasonException;
import com.wanted.momocity.user.domain.model.Role;
import com.wanted.momocity.user.domain.model.Status;
import com.wanted.momocity.user.domain.model.UpdateUserInfoData;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserCommandService implements UserCommandUsecase {

    private final UserRepository userRepository;
    private final UserPolicy userPolicy;
    private final PasswordEncodePort passwordEncodePort;
    private final S3UploadPort s3UploadPort;
    private final ApplicationEventPublisher eventPublisher;
    private final GetItemUrlPort getItemUrlPort;
    private final GoogleDriveUploadPort googleDriveUploadPort;


    @Override
    public String registerNickname(NicknameRegisterCommand command) {
        userPolicy.nicknamePolicy(command.nickname());
        userRepository.registerNickname(command.userId(), command.nickname());
        log.info("[user] 닉네임 등록 완료 | userId={} | nickname={}", command.userId(), command.nickname());
        return command.nickname();
    }

    @Override
    public void updateUserInfo(UpdateUserInfoCommand command) {

        // 프사 이름으로 프사 url 가져오기
        String url = getItemUrlPort.getItemUrl(command.itemName(),command.userId());

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

        userRepository.updateUserInfo(new UpdateUserInfoData(
                command.userId(),
                url,
                command.nickname(),
                encodedPassword
        ));
    }

    // 강사 신청
    @Override
    public void teacherApply(TeacherApplyCommand command) {

        String name = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."))
                .getName();

        if (userRepository.checkTeacherAvailable(command.userId(), Role.TEACHER, List.of(Status.PENDING, Status.ACTIVE))) {
            throw new DomainRuleViolationException("강사 신청 중이거나 이미 강사입니다.");
        }

        // 기존 닉네임이랑 새로운 닉네임이 다르면 policy로 중복 검증
        if(!command.currentNickname().equals(command.nickname())){
            userPolicy.nicknamePolicy(command.nickname());
        }
        userPolicy.teacherProofPolicy(command.proof());
        String proofKey = s3UploadPort.upload(command.proof(), "teacher_proof");

        userRepository.teacherApply(command.userId(),command.nickname(),command.category(),proofKey);

        // 드라이브에 업로드
        String originalFileName = command.proof().getOriginalFilename();
        String driveFileName = name + " - " + command.category().name() + " - " + originalFileName;
        googleDriveUploadPort.uploadGoogleDrive(command.proof(), driveFileName);

        log.info("[teacherApply] 강사 신청 완료 | userId={} | role=TEACHER", command.userId());

    }

    // 강사승인
    @Override
    public void approve(ApproveTeacherCommand command) {

        command.userId().forEach(userId -> {
            String email = userRepository.findById(userId)
                    .orElseThrow(()-> new UserNotFoundException("사용자를 찾을 수 없습니다."))
                    .getEmail();

            // PENDING 상태인지 검증
            Status status = userRepository.findStatusById(userId);
            if (status != Status.PENDING) {
                throw new DomainRuleViolationException("강사 신청 중인 사용자가 아닙니다.");
            }

            String categoryProfileImage = userRepository.findCategoryById(userId)
                    .getCategoryProfileImage();

            userRepository.updateAfterApply(userId, Role.TEACHER, Status.ACTIVE,categoryProfileImage);
            log.info("[teacher] 강사 승인 처리 | userId={}", userId);
            eventPublisher.publishEvent(new TeacherApplicationEvent(email, Status.ACTIVE, null));
        });
    }

    // 강사거절
    @Override
    public void reject(RejectTeacherCommand command) {

        if (command.reason() == null || command.reason().length() < 10) {
            throw new InvalidReasonException("반려 사유는 최소 10자 이상이어야 합니다.");
        }

        String email = userRepository.findById(command.userId())
                .orElseThrow(()-> new UserNotFoundException("사용자를 찾을 수 없습니다."))
                .getEmail();

        // PENDING 상태인지 검증
        Status status = userRepository.findStatusById(command.userId());
        if (status != Status.PENDING) {
            throw new DomainRuleViolationException("강사 신청 중인 사용자가 아닙니다.");
        }

        userRepository.updateAfterApply(command.userId(), Role.STUDENT, Status.REJECTED,null);
        log.info("[teacher] 강사 반려 처리 | userId={} | reason={}", command.userId(), command.reason());
        eventPublisher.publishEvent(new TeacherApplicationEvent(email, Status.REJECTED, command.reason()));
    }


    // 밤티 알림 설정
    @Override
    public boolean setAlarm(Long userId) {
       return userRepository.setAlarm(userId);
    }

    // 강사 포기
    @Override
    public void teacherGiveup(Long userId) {
        userRepository.changeStatus(userId,Status.ACTIVE);
    }


    // 회원탈퇴 (소프트 딜리트)
    @Override
    public void softDeleteUser(Long userId) {
        userRepository.changeStatusAndNickname(userId, Status.DELETED, null);
    }
}