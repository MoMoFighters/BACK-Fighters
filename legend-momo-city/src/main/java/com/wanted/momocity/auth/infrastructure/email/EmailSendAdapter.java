package com.wanted.momocity.auth.infrastructure.email;

import com.wanted.momocity.auth.application.port.EmailSendPort;
import com.wanted.momocity.auth.domain.exception.EmailSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSendAdapter implements EmailSendPort {

    private final JavaMailSender mailSender;

    @Override
    public void send(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[인증] 이메일 인증 코드");
            message.setText("인증 코드: " + code + "\n\n3분 내에 입력해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}