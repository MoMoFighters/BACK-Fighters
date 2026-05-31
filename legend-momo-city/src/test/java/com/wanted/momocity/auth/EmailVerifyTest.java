package com.wanted.momocity.auth;

import com.wanted.momocity.auth.application.command.EmailVerifyCommand;
import com.wanted.momocity.auth.application.port.EmailCodePort;
import com.wanted.momocity.auth.application.service.AuthQueryService;
import com.wanted.momocity.auth.domain.exception.InvalidVerificationCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerifyService 테스트")
public class EmailVerifyTest {

    @Mock private EmailCodePort emailCodePort;

    @InjectMocks
    private AuthQueryService authQueryService;

    @BeforeEach
    void setup() {
        assertNotNull(authQueryService);
    }

    @Test
    @DisplayName("인증코드 만료")
    void 이메일_인증코드_만료() {
        // given
        given(emailCodePort.find("test@test.com")).willReturn(null);

        // when & then
        InvalidVerificationCodeException exception = assertThrows(
                InvalidVerificationCodeException.class,
                () -> authQueryService.emailVerify(new EmailVerifyCommand("test@test.com", "123456"))
        );

        assertEquals("인증 코드가 만료되었습니다. 재발송 버튼을 눌러 인증코드를 다시 발급받아 입력해주세요.", exception.getMessage());
    }

    @Test
    @DisplayName("인증코드 불일치")
    void 이메일_인증코드_불일치() {
        // given
        given(emailCodePort.find("test@test.com")).willReturn("123456");

        // when & then
        InvalidVerificationCodeException exception = assertThrows(
                InvalidVerificationCodeException.class,
                () -> authQueryService.emailVerify(new EmailVerifyCommand("test@test.com", "999999"))
        );

        assertEquals("인증 코드가 올바르지 않습니다.", exception.getMessage());
    }
}