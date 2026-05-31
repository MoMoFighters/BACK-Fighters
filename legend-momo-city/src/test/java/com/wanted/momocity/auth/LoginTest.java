package com.wanted.momocity.auth;

import com.wanted.momocity.auth.application.command.LoginCommand;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.auth.application.port.RedisRefreshTokenPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.service.AuthCommandService;
import com.wanted.momocity.auth.domain.exception.InactiveUserException;
import com.wanted.momocity.auth.domain.exception.InvalidCredentialsException;
import com.wanted.momocity.auth.domain.exception.TempPasswordExpiredException;
import com.wanted.momocity.auth.domain.model.Status;
import com.wanted.momocity.auth.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService 테스트")
public class LoginTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenProviderPort tokenProviderPort;
    @Mock private LoadUserPort loadUserPort;
    @Mock private RedisRefreshTokenPort redisRefreshTokenPort;
    @Mock private EmailCodePort emailCodePort;

    @InjectMocks
    private AuthCommandService loginService;

    @BeforeEach
    void setup() {
        assertNotNull(loginService);
    }

    @Test
    @DisplayName("회원가입 되지 않은 이메일")
    void 회원가입_되지_않은_이메일() {
        // given
        given(loadUserPort.findByEmail("none@test.com")).willReturn(Optional.empty());

        // when & then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(new LoginCommand("none@test.com", "password123!"))
        );

        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 불일치")
    void 비밀번호_불일치로_로그인_실패() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(loadUserPort.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("bad credentials"));

        // when & then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(new LoginCommand("test@test.com", "wrongpassword!"))
        );

        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("active가 아닌 계정")
    void active_아닌_계정_로그인_실패() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getStatus()).willReturn(Status.PENDING);
        given(loadUserPort.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(authenticationManager.authenticate(any())).willReturn(mock(Authentication.class));

        // when & then
        InactiveUserException exception = assertThrows(
                InactiveUserException.class,
                () -> loginService.login(new LoginCommand("test@test.com", "password123!"))
        );

        assertEquals("로그인이 제한된 계정입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("임시비밀번호 만료")
    void 임시비밀번호_만료로_로그인_실패() {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getStatus()).willReturn(Status.ACTIVE);
        given(user.getIsTempPwd()).willReturn(true);
        given(loadUserPort.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(authenticationManager.authenticate(any())).willReturn(mock(Authentication.class));
        given(emailCodePort.isTempPasswordVerified("test@test.com")).willReturn(false);

        // when & then
        TempPasswordExpiredException exception = assertThrows(
                TempPasswordExpiredException.class,
                () -> loginService.login(new LoginCommand("test@test.com", "password123!"))
        );

        assertEquals("임시 비밀번호가 만료되었습니다. 다시 발급해주세요.", exception.getMessage());
    }
}