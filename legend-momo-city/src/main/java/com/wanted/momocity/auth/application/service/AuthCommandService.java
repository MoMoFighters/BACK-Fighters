package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.*;
import com.wanted.momocity.auth.application.policy.SignupPolicy;
import com.wanted.momocity.auth.application.port.*;
import com.wanted.momocity.auth.application.usecase.AuthCommandUsecase;
import com.wanted.momocity.auth.domain.event.SignupCompletedEvent;
import com.wanted.momocity.auth.domain.exception.*;
import com.wanted.momocity.auth.domain.model.Status;
import com.wanted.momocity.auth.domain.model.User;
import com.wanted.momocity.auth.domain.model.UserOauth;
import com.wanted.momocity.auth.domain.repository.UserOauthRepository;
import com.wanted.momocity.auth.domain.repository.UserRepository;
import com.wanted.momocity.auth.presentation.api.response.EmailSendResponse;
import com.wanted.momocity.auth.presentation.api.response.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@Transactional
public class AuthCommandService implements AuthCommandUsecase {

    private final UserRepository userRepository;
    private final LoadUserPort loadUserPort;
    private final SignupPolicy signupPolicy;
    private final UserOauthRepository userOauthRepository;

    private final PasswordEncoder passwordEncoder;
    private final BlacklistPort blacklistPort;
    private final AuthenticationManager authenticationManager;
    private final TokenProviderPort tokenProviderPort;
    private final RedisRefreshTokenPort redisRefreshTokenPort;

    private final ApplicationEventPublisher eventPublisher;

    private final EmailCodePort emailCodePort;
    private final EmailSendPort emailSendPort;

    private final Map<String, OAuthClientPort> oAuthClientPorts;

    private final UpdatePasswordPort updatePasswordPort;
    private final PasswordEncodePort passwordEncodePort;

    private static final long EXPIRES_IN_SECONDS = 180L; // 임시 비번 만료시간 3분


    public AuthCommandService(
            LoadUserPort loadUserPort, SignupPolicy signupPolicy, @Qualifier("kakaoOAuthClient")
            OAuthClientPort kakaoOAuthClientPort,
            @Qualifier("googleOAuthClient")
            OAuthClientPort googleOAuthClientPort,
            UserRepository userRepository,
            UserOauthRepository userOauthRepository, PasswordEncoder passwordEncoder, BlacklistPort blacklistPort, AuthenticationManager authenticationManager,
            TokenProviderPort tokenProviderPort, ApplicationEventPublisher eventPublisher, RedisRefreshTokenPort redisRefreshTokenPort, EmailCodePort emailCodePort, EmailSendPort emailSendPort, UpdatePasswordPort updatePasswordPort, PasswordEncodePort passwordEncodePort
    ) {
        this.loadUserPort = loadUserPort;
        this.signupPolicy = signupPolicy;
        this.passwordEncoder = passwordEncoder;
        this.blacklistPort = blacklistPort;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
        this.redisRefreshTokenPort = redisRefreshTokenPort;
        this.emailCodePort = emailCodePort;
        this.emailSendPort = emailSendPort;
        this.updatePasswordPort = updatePasswordPort;
        this.passwordEncodePort = passwordEncodePort;
        this.oAuthClientPorts = Map.of(
                "KAKAO", kakaoOAuthClientPort,
                "GOOGLE", googleOAuthClientPort
        );
        this.userRepository = userRepository;
        this.userOauthRepository = userOauthRepository;
        this.tokenProviderPort = tokenProviderPort;
    }


    // 강사 회원가입
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

        log.info("[signup] 회원가입 완료 | email={} | role=TEACHER", command.email());
    }

    // 학생 회원가입
    @Override
    public void signup(StudentSignupCommand command) {

        // 정책 확인
        signupPolicy.ensureEligible(command.email());

        // 이메일(id), 비밀번호, 이름 넘겨서 새로운 학생 자바 객체 생성
        User user = userRepository.register(User.studentRegister(command.email(), passwordEncoder.encode(command.password()), command.name()));

        // 이메일 인증 하고서 인증됨 의 상태를 지움
        emailCodePort.deleteVerified(command.email());

        // 회원가입 하고서 이벤트 발행 - 나와의 채팅 생성용
        eventPublisher.publishEvent(new SignupCompletedEvent(user.getId()));

        log.info("[signup] 회원가입 완료 | email={} | role=STUDENT", command.email());

    }

    // 로그인
    @Override
    public LoginResponse login(LoginCommand command) {

        // email로 유저 먼저 조회해서 id 꺼내기
        User user = loadUserPort.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 이메일/비밀번호로 사용자 인증
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), command.password())
            );

        }catch (BadCredentialsException e){
            log.warn("[login] 로그인 실패 | email={} | 사유= 인증 실패", command.email());
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() != Status.ACTIVE) {
            log.warn("[login] 비활성 계정 로그인 시도 | email={} | status={}", command.email(), user.getStatus());
            String message = switch (user.getStatus()) {
                case PENDING -> "강사 승인 대기중입니다.";
                case REJECTED -> "강사 신청이 반려되었습니다. 증빙자료를 다시 제출해주세요.";
                case BANNED -> "정지된 계정입니다.";
                default -> "해당 계정은 현재 로그인이 불가능한 상태입니다.";
            };
            throw new InactiveUserException(message, user.getStatus());
        }

        if (user.getIsTempPwd() && !emailCodePort.isTempPasswordVerified(command.email())) {
            throw new TempPasswordExpiredException("임시 비밀번호가 만료되었습니다. 다시 발급해주세요.");
        }

        // 인증 성공 후 액세스 토큰 발급
        String accessToken = user.getIsTempPwd()
                ? tokenProviderPort.createTempAccessToken(authentication)
                : tokenProviderPort.createAccessToken(authentication);

        // 리프레시 토큰 발급
        String refreshToken = tokenProviderPort.createRefreshToken(String.valueOf(user.getId()));

        // 기존 리프레시 토큰 삭제 후 새로 저장
        redisRefreshTokenPort.save(
                String.valueOf(user.getId()),
                refreshToken,
                Instant.now().plusMillis(tokenProviderPort.getRefreshTokenValidityMilliseconds())
        );

        // 컨트롤러로 보내서 프론트에게 전달할 수 있도록 리턴
        long accessTokenExpiry = user.getIsTempPwd()
                ? 3 * 60  // 3분
                : tokenProviderPort.getAccessTokenValidityMilliseconds() / 1000;

        log.info("[login] 로그인 성공 | userId={} | isTempPwd={}", user.getId(), user.getIsTempPwd());
        return new LoginResponse(accessToken, refreshToken, user.getStatus(), accessTokenExpiry);
    }

    // 로그아웃
    @Override
    public void logout(LogoutCommand command) {
        // RefreshToken 삭제
        redisRefreshTokenPort.deleteByToken(command.refreshToken());

        // AccessToken 블랙리스트 등록 (로그아웃 하고 남은 만료시간만큼만 블랙리스트로)
        long remainingMillis = tokenProviderPort.getRemainingMillis(command.accessToken());
        if (remainingMillis > 0) {
            blacklistPort.addBlacklist(command.accessToken(), remainingMillis);
        }

        log.info("[logout] 로그아웃 완료 | remainingMillis={}", remainingMillis);
    }

    @Override
    public LoginResponse socialLogin(SocialLoginCommand command) {
        // provider가 카카오면 kakaoOAuthClientPort
        // provider가 구글이면 googleOAuthClientPort
        OAuthClientPort oAuthClientPort = oAuthClientPorts.get(command.provider());
        // 거기서 api에 요청 두번 보내서 access 토큰 요청 + 유저 정보 요청하고 사용자 정도 담음
        OAuthUserInfoCommand oAuthUserInfo = oAuthClientPort.getUserInfo(command.code());

        // 이미 소셜 로그인 한 사람인지 확인
        User user = userOauthRepository.findByProviderAndProviderId(command.provider(), oAuthUserInfo.providerId())
                .map(UserOauth::getUser)
                .orElseGet(() -> registerNewUser(command.provider(), oAuthUserInfo));

        log.info("[social] 소셜 로그인 인증 완료 | provider={} | userId={}", command.provider(), user.getId());

        // 인증 성공하면 JWT 토큰 발급
        String accessToken = tokenProviderPort.createAccessToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );
        String refreshToken = tokenProviderPort.createRefreshToken(
                String.valueOf(user.getId())
        );

        // RefreshToken을 Redis에 저장
        redisRefreshTokenPort.save(
                String.valueOf(user.getId()),
                refreshToken,
                Instant.now().plusMillis(tokenProviderPort.getRefreshTokenValidityMilliseconds())
        );


        // 응답
        return new LoginResponse(accessToken, refreshToken, user.getStatus(),
                tokenProviderPort.getAccessTokenValidityMilliseconds());
    }

    // 새로운 유저 db에 등록
    private User registerNewUser(String provider, OAuthUserInfoCommand oAuthUserInfoCommand) {
        try {
            User newUser = userRepository.register(
                    User.oAuthRegister(oAuthUserInfoCommand.email(), oAuthUserInfoCommand.name())
            );
            userOauthRepository.save(
                    UserOauth.create(newUser, provider, oAuthUserInfoCommand.providerId())
            );
            eventPublisher.publishEvent(new SignupCompletedEvent(newUser.getId()));
            return newUser;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("이미 해당 이메일로 가입된 계정이 있습니다. 자체 로그인을 이용하거나 다른 이메일을 이용해주세요.");
        }
    }

    @Override
    public EmailSendResponse emailSend(EmailSendCommand command) {
        if (loadUserPort.findByEmail(command.email()).isPresent()) {
            log.warn("[email] 중복 이메일 가입 시도 | email={}", command.email());
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }

        String code = generateCode();
        emailCodePort.save(command.email(), code, EXPIRES_IN_SECONDS);
        emailSendPort.send(command.email(), code);
        log.info("[email] 인증코드 발송 완료 | email={}", command.email());
        return new EmailSendResponse(EXPIRES_IN_SECONDS);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Override
    public void sendTempPassword(EmailSendCommand command) {

        if (!loadUserPort.findByEmail(command.email()).isPresent()) {
            log.warn("[temp-pwd] 미가입 이메일 임시비밀번호 요청 | email={}", command.email());
            throw new UserNotFoundException("가입된 이메일이 아닙니다.");
        }

        String tempPassword = generateTempPassword();
        String encodedPassword = passwordEncodePort.encode(tempPassword);

        updatePasswordPort.updatePassword(command.email(), encodedPassword);
        emailCodePort.saveTempPassword(command.email(), EXPIRES_IN_SECONDS); // redis에 만료시간 저장
        emailSendPort.sendTempPassword(command.email(), tempPassword); // 이메일 발송
        log.info("[temp-pwd] 임시비밀번호 발급 완료 | email={}", command.email());
    }

    private String generateTempPassword() {
        return String.format("%08d", new Random().nextInt(100000000));
    }

}
