package com.wanted.momocity.auth.infrastructure.email;

import com.wanted.momocity.auth.application.port.EmailSendPort;
import com.wanted.momocity.auth.domain.exception.EmailSendException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSendAdapter implements EmailSendPort {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void send(String toEmail, String code) {
        sendEmail(toEmail, code, "인증 코드");
    }

    @Async
    @Override
    public void sendTempPassword(String toEmail, String tempPassword) {
        sendEmail(toEmail, tempPassword, "임시 비밀번호");
    }

    private void sendEmail(String toEmail, String code, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[MOMO CITY] " + type + " 안내");
            helper.setText(buildEmailTemplate(code, type), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private String buildEmailTemplate(String code, String type) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px; background-color: #f9f9f9;">
                <div style="background-color: #ffffff; border-radius: 8px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                    <h1 style="color: #333333; font-size: 24px; margin-bottom: 8px;">MOMO CITY</h1>
                    <hr style="border: none; border-top: 2px solid #f0f0f0; margin: 20px 0;">
                    <p style="color: #555555; font-size: 16px;">안녕하세요!</p>
                    <p style="color: #555555; font-size: 16px;">아래 %s을 확인해주세요.</p>
                    <div style="background-color: #f4f4f4; border-radius: 6px; padding: 20px; text-align: center; margin: 30px 0;">
                        <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #222222;">%s</span>
                    </div>
                    <p style="color: #888888; font-size: 14px;">⏰ 3분 후 만료됩니다.</p>
                    <p style="color: #888888; font-size: 14px;">본인이 요청하지 않았다면 이 이메일을 무시해주세요.</p>
                    <hr style="border: none; border-top: 2px solid #f0f0f0; margin: 30px 0;">
                    <p style="color: #aaaaaa; font-size: 12px; text-align: center;">© 2026 MOMO CITY. All rights reserved.</p>
                </div>
            </div>
            """.formatted(type, code);
    }
}