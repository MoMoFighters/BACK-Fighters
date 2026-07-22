package com.wanted.momocity.user.application.service;

import com.wanted.momocity.auth.application.port.PasswordEncodePort;
import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.global.domain.model.Category;
import com.wanted.momocity.user.application.port.GetItemUrlPort;
import com.wanted.momocity.user.application.port.GoogleDriveUploadPort;
import com.wanted.momocity.user.application.port.ReportRedisPort;
import com.wanted.momocity.user.domain.event.DriveUploadEvent;
import com.wanted.momocity.user.domain.event.ReportRedisEvent;
import com.wanted.momocity.user.domain.event.TeacherApplicationEvent;
import com.wanted.momocity.user.domain.exception.AlreadySuspendedException;
import com.wanted.momocity.user.domain.exception.UserNotFoundException;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.user.application.command.*;
import com.wanted.momocity.user.application.policy.UserPolicy;
import com.wanted.momocity.user.application.usecase.UserCommandUsecase;
import com.wanted.momocity.user.domain.exception.InvalidReasonException;
import com.wanted.momocity.user.domain.model.*;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


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
    private final ReportRedisPort reportRedisPort;

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
        String url = null;
        if (command.itemName() != null) {
            url = getItemUrlPort.getItemUrl(command.itemName(), command.userId());
        }

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

        userRepository.teacherApply(command.userId(),command.nickname(),command.category(),proofKey,LocalDateTime.now());

        // 드라이브에 업로드
        String originalFileName = command.proof().getOriginalFilename();
        String driveFileName = name + " - " + command.category().name() + " - " + originalFileName;
        eventPublisher.publishEvent(new DriveUploadEvent(driveFileName, proofKey, command.userId()));

        /*comment
        *  MultipartFile은 HTTP 요청이 살아있는 동안만 접근 가능한데
        *  비동기는 요청이 끝난 후 별도 스레드에서 실행되니까 그때는 이미 파일 데이터가 사라진 상태
        *  그래서 S3에는 이미 파일이 올라가 있으니까 proofKey로 S3에서 파일을 가져와 그거로 비동기 스레드에서 드라이브에 원본파일ㅇ을 업로드*/
        log.info("[teacherApply] 강사 신청 완료 | userId={} | role=TEACHER", command.userId());

    }

    // 강사승인
    @Override
    @CacheEvict(value = "adminUserList", allEntries = true)
    public void approve(ApproveTeacherCommand command) {

        List<Long> userIds = command.userId();

        List<User> users = userRepository.findAllByIdsForApprove(userIds);

        Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
        List<Long> notFoundIds = userIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            log.warn("[teacher] 존재하지 않는 사용자 제외 | notFoundIds={}", notFoundIds);
        }

        List<Long> notPendingIds = users.stream()
                .filter(user -> user.getStatus() != Status.PENDING)
                .map(User::getId)
                .toList();

        if (!notPendingIds.isEmpty()) {
            log.warn("[teacher] 강사 신청 중이 아닌 사용자 제외 | notPendingIds={}", notPendingIds);
        }

        List<User> approvableUsers = users.stream()
                .filter(user -> user.getStatus() == Status.PENDING)
                .toList();

        // 카테고리별로 그룹핑 -> 카테고리당 1번의 벌크 UPDATE (최대 5번)
        Map<Category, List<Long>> idsByCategory = approvableUsers.stream()
                .collect(Collectors.groupingBy(
                        User::getCategory,
                        Collectors.mapping(User::getId, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();
        idsByCategory.forEach((category, ids) ->
                userRepository.bulkUpdateAfterApply(ids, Role.TEACHER, Status.ACTIVE,
                        category.getCategoryProfileImage(), now)
        );

        // 승인 처리 후 이벤트는 유저별로 발행 (이메일 발송은 개별 처리)
        approvableUsers.forEach(user -> {
            log.info("[teacher] 강사 승인 처리 | userId={}", user.getId());
            eventPublisher.publishEvent(new TeacherApplicationEvent(user.getEmail(), Status.ACTIVE, null));
        });
    }


    // 강사거절
    @Override
    @CacheEvict(value = "adminUserList", allEntries = true)
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
    @CacheEvict(value = "adminUserList", allEntries = true)
    public void softDeleteUser(Long userId) {
        userRepository.changeStatusAndNickname(userId, Status.DELETED, null);
    }

    // 사용자 신고 횟수 +
    @Override
    @CacheEvict(value = "adminUserList", allEntries = true)
    public LocalDateTime plusReportCount(Long userId) {

        // 이미 정지 상태이면 더 신고 + 못 시킴
        if (userPolicy.isSuspended(userId)) {
            throw new AlreadySuspendedException("이미 정지된 사용자입니다.");
        }

        // 일단 신고 횟수 +1 하고 그건 리턴 받고
        Long count = userRepository.plusReportCount(userId);
        // 리턴 받은 그 신고 카운트를 기반으로 사용자 상태 변경
        CheckStatusResult result = reportApply(count);
        userRepository.reportApply(userId, result.status(), result.suspendedUntil());

        // 신고 처리 시각 Redis에 저장
        eventPublisher.publishEvent(new ReportRedisEvent(userId, true));

        log.info("[user] 신고 처리 | userId={} | count={} | status={} | suspendedUntil={}",
                userId, count, result.status(), result.suspendedUntil());

        return result.suspendedUntil();
    }

    private CheckStatusResult reportApply(Long count) {
        LocalDateTime midnight = LocalDateTime.now()
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(); // 정지 기간을 정지 마지막날 자정으로 설정
        // 그렇게 해야 매일 자정마다 도는 스케줄러에 맞춰 정지를 풀어줄 수 있음

        return switch (count.intValue()) {
            case 1 -> new CheckStatusResult(Status.BANNED, midnight.plusWeeks(1));
            case 2 -> new CheckStatusResult(Status.BANNED, midnight.plusMonths(1));
            default -> new CheckStatusResult(Status.BLACK, null);
        };
    }

    // 사용자 신고 횟수 -
    @Override
    @CacheEvict(value = "adminUserList", allEntries = true)
    public void minusReportCount(Long userId) {
        // Active인 사람은 - 못 시킴
        if (userPolicy.isActive(userId)) {
            throw new AlreadySuspendedException("활성 사용자의 정지 횟수는 줄일 수 없습니다.");
        }

        if (!reportRedisPort.existsReportTime(userId)) {
            throw new DomainRuleViolationException("신고 후 24시간이 경과하여 복구할 수 없습니다.");
        }

        userRepository.minusReportCount(userId);
        eventPublisher.publishEvent(new ReportRedisEvent(userId, false));
        log.info("[user] 사용자 제재 복구 완료 | userId={}", userId);
    }
}

