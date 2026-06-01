package com.wanted.momocity.user.infrastructure.email;

import com.wanted.momocity.user.application.port.UserEmailSendPort;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEmailSendAdapter implements UserEmailSendPort {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendTeacherResult(String toEmail, String status, String reason) {
        String type = "ACTIVE".equals(status) ? "강사 신청 승인" : "강사 신청 반려";
        String content = "ACTIVE".equals(status)
                ? "강사 신청이 승인되었습니다.🎉"
                : "강사 신청이 반려되었습니다.<br><hr>반려 사유: " + reason;
        sendEmail(toEmail, content, type);
    }

    private void sendEmail(String toEmail, String content, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[MOMO CITY] " + type + " 안내");
            helper.setText(buildEmailTemplate(content, type), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    private String buildEmailTemplate(String content, String type) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 40px 20px; background-color: #f9f9f9;">
                <div style="background-color: #ffffff; border-radius: 8px; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                    <h1 style="color: #333333; font-size: 24px; margin-bottom: 8px;">MOMO CITY</h1>
                    <hr style="border: none; border-top: 2px solid #f0f0f0; margin: 20px 0;">
                    <p style="color: #555555; font-size: 16px;">강사 신청 결과를 안내드립니다.</p>
                    <div style="background-color: #f4f4f4; border-radius: 6px; padding: 20px; text-align: center; margin: 30px 0;">
                        <p style="font-size: 16px; color: #222222;">%s</p>
                    </div>
                    <hr style="border: none; border-top: 2px solid #f0f0f0; margin: 30px 0;">
                    <p style="color: #aaaaaa; font-size: 12px; text-align: center;">© 2026 MOMO CITY. All rights reserved.</p>
                </div>
            </div>
            """.formatted(content);
    }
}