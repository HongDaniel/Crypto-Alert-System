package com.cryptoAlert;

import com.cryptoAlert.dto.request.EmailRequest;
import com.cryptoAlert.entity.AlertSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * 간단한 HTML 이메일 테스트 메소드
     */
    public void sendSimpleTestEmail() {
        try {
            log.info("HTML 이메일 테스트 시작");
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo("test@example.com");
            helper.setSubject("[테스트] 간단한 HTML 이메일");
            helper.setFrom("danny.jung1223@gmail.com");
            
            String htmlBody = createTestEmailBody();
            helper.setText(htmlBody, true);
            
            mailSender.send(mimeMessage);
            log.info("HTML 이메일 테스트 성공 - 수신자: test@example.com");
            
        } catch (Exception e) {
            log.error("HTML 이메일 테스트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }
    
    private String createTestEmailBody() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>테스트 이메일</title>
            </head>
            <body>
                <h1 style="color: #2E8B57;">🎉 HTML 이메일 테스트 성공!</h1>
                <p style="font-size: 16px; color: #333;">
                    이 이메일이 정상적으로 HTML 형태로 렌더링되어 보인다면<br>
                    <strong>MimeMessage + MimeMessageHelper</strong> 설정이 올바르게 작동하는 것입니다.
                </p>
                <div style="background-color: #f0f8ff; padding: 15px; border-left: 4px solid #4169E1; margin: 20px 0;">
                    <h3 style="color: #4169E1; margin-top: 0;">테스트 정보</h3>
                    <ul>
                        <li>발송 시간: %s</li>
                        <li>발송자: danny.jung1223@gmail.com</li>
                        <li>수신자: test@example.com</li>
                        <li>이메일 형식: HTML</li>
                    </ul>
                </div>
                <p style="color: #666; font-size: 14px;">
                    이 테스트가 성공하면 Thymeleaf 템플릿도 정상 작동할 것입니다.
                </p>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now());
    }

    /**
     * HTML 이메일 발송 메소드
     */
    public void sendEmail(EmailRequest emailRequest) {
        try {
            log.info("이메일 발송 시작 - 수신자: {}", emailRequest.getToEmail());
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(emailRequest.getToEmail());
            helper.setSubject(emailRequest.getSubject());
            helper.setFrom("danny.jung1223@gmail.com");
            helper.setText(emailRequest.getBody(), true);
            
            mailSender.send(mimeMessage);
            log.info("이메일 발송 성공 - 수신자: {}, 제목: {}", 
                    emailRequest.getToEmail(), emailRequest.getSubject());
                    
        } catch (Exception e) {
            log.error("이메일 발송 실패 - 수신자: {}, 오류: {}", 
                    emailRequest.getToEmail(), e.getMessage(), e);
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }

    /**
     * AlertSetting을 기반으로 EmailRequest 생성
     */
    public EmailRequest createEmailRequest(AlertSetting alertSetting, String email) {
        log.debug("이메일 요청 생성 시작 - threshold: {}, email: {}", 
                alertSetting.getThreshold(), email);
        
        String alertCategory = calculateAlertCategory(alertSetting.getThreshold());
        String subject = "[Crypto Alert] " + getEmailSubjectByAlertCategory(alertCategory);
        String body = generateEmailBody(alertCategory, alertSetting, email);
        
        log.debug("이메일 요청 생성 완료 - 카테고리: {}, 제목: {}", alertCategory, subject);
        return new EmailRequest(email, subject, body);
    }

    private String generateEmailBody(String category, AlertSetting setting, String email) {
        log.debug("이메일 본문 생성 시작 - 카테고리: {}, 템플릿: {}", 
                category, getTemplateNameByAlertCategory(category));
        
        Context context = createThymeleafContext(category, setting);
        
        try {
            String templateName = getTemplateNameByAlertCategory(category);
            String body = templateEngine.process(templateName, context);
            
            log.debug("템플릿 처리 성공 - 본문 길이: {}", body != null ? body.length() : 0);
            return body;
            
        } catch (Exception e) {
            log.error("템플릿 처리 실패 - 카테고리: {}, 오류: {}", category, e.getMessage(), e);
            return createFallbackEmailBody(setting, category);
        }
    }
    
    private Context createThymeleafContext(String category, AlertSetting setting) {
        Context context = new Context();
        context.setVariable("index", setting.getThreshold());
        context.setVariable("classification", category);
        // 시간을 "2025-10-17 09:00" 형태로 포맷팅
        String formattedTime = setting.getUpdatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        context.setVariable("time", formattedTime);
        return context;
    }
    
    private String createFallbackEmailBody(AlertSetting setting, String category) {
        return String.format("""
            <html>
            <body>
                <h1>Crypto Alert</h1>
                <p>Threshold: %d</p>
                <p>Category: %s</p>
                <p>Time: %s</p>
            </body>
            </html>
            """, setting.getThreshold(), category, setting.getUpdatedAt());
    }

    /**
     * Fear & Greed Index 값에 따른 알림 카테고리 계산
     */
    private String calculateAlertCategory(int threshold) {
        if (threshold >= 75) {
            return "Extreme Greed";
        } else if (threshold >= 55) {
            return "Greed";
        } else if (threshold >= 45) {
            return "Neutral";
        } else if (threshold >= 25) {
            return "Fear";
        } else {
            return "Extreme Fear";
        }
    }

    /**
     * 알림 카테고리에 따른 이메일 제목 생성
     */
    private String getEmailSubjectByAlertCategory(String category) {
        return switch (category) {
            case "Extreme Greed" -> "이제 팔아야 해!!";
            case "Greed" -> "슬슬 분할매도 하자!";
            case "Neutral" -> "관망이 필요해!";
            case "Fear" -> "매수 기회야!";
            case "Extreme Fear" -> "지금 완전 세일 기간이야!!";
            default -> "새로운 알림!";
        };
    }

    /**
     * 알림 카테고리에 따른 Thymeleaf 템플릿 이름 반환
     */
    private String getTemplateNameByAlertCategory(String category) {
        return switch (category) {
            case "Extreme Greed" -> "email/extreme-greed-template";
            case "Greed" -> "email/greed-template";
            case "Neutral" -> "email/neutral-template";
            case "Fear" -> "email/fear-template";
            case "Extreme Fear" -> "email/extreme-fear-template";
            default -> "email/default-template";
        };
    }
}
