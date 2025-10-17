package com.cryptoAlert.api.controller;

import com.cryptoAlert.AlertHistoryService;
import com.cryptoAlert.AlertSettingService;
import com.cryptoAlert.CryptoFearIndexService;
import com.cryptoAlert.EmailService;
import com.cryptoAlert.SmsService;
import com.cryptoAlert.dto.request.EmailRequest;
import com.cryptoAlert.dto.request.SmsRequest;
import com.cryptoAlert.dto.response.CryptoFearIndexResponse;
import com.cryptoAlert.entity.AlertHistory;
import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.repository.UserRepository;

import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// import javax.mail.MessagingException;


@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final EmailService emailService;
    private final SmsService smsService;
    private final AlertHistoryService alertHistoryService;
    private final AlertSettingService alertSettingService;
    private final CryptoFearIndexService cryptoFearIndexService;
    private final UserRepository userRepository;

    @Operation(summary = "알림 발송", description = "AlertSetting 객체를 받아서 이메일/SMS 알림을 발송합니다")
    @PostMapping("/send")
    public ResponseEntity<String> sendAlert(@RequestBody AlertSetting alertSetting, @AuthenticationPrincipal User user) {
        // 실제 로그인된 사용자 정보를 userRepository에서 조회
        if (user == null) {
            log.warn("User가 null입니다. 기본 사용자를 사용합니다.");
            try {
                user = userRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("기본 사용자를 찾을 수 없습니다. ID: 1"));
                log.info("기본 사용자 사용: {}", user.getEmail());
            } catch (Exception e) {
                log.error("기본 사용자 조회 실패: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("Default user not found");
            }
        } else {
            // 로그인된 사용자의 최신 정보를 userRepository에서 조회
            try {
                final Long userId = user.getId();
                user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));
                log.info("로그인된 사용자 정보 조회: {}", user.getEmail());
            } catch (Exception e) {
                log.error("사용자 조회 실패: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("User not found");
            }
        }
        log.info("Alert Send API 호출됨 - User: {}, AlertSetting: {}", user.getEmail(), alertSetting);
        
        try {
            // 중복 체크: 같은 사용자가 동일한 threshold와 alertType을 가진 설정이 있는지 확인
            Optional<AlertSetting> existingAlert = alertSettingService.findByUserIdAndThresholdAndAlertType(
                    user.getId(), alertSetting.getThreshold(), alertSetting.getAlertType());
            
            AlertSetting savedAlertSetting;
            if (existingAlert.isPresent()) {
                log.info("기존 AlertSetting 사용: ID={}", existingAlert.get().getId());
                savedAlertSetting = existingAlert.get();
            } else {
                // AlertSetting을 먼저 저장 (transient 상태 해결)
                AlertSetting newAlertSetting = new AlertSetting(
                    user,
                    alertSetting.getThreshold(),
                    alertSetting.getAlertType(),
                    alertSetting.isEmail(),
                    alertSetting.isSms()
                );
                savedAlertSetting = alertSettingService.saveAlertSetting(newAlertSetting);
                log.info("AlertSetting 저장됨: ID={}", savedAlertSetting.getId());
            }
            
            // 실제 Fear & Greed Index 가져오기
            CryptoFearIndexResponse fearIndexResponse = cryptoFearIndexService.getCryptoFearIndex();
            int currentIndex = Integer.parseInt(fearIndexResponse.getData().get(0).getValue());
            log.info("현재 Fear & Greed Index: {}", currentIndex);
            
            // 조건 확인: 현재 인덱스가 설정된 조건을 만족하는지 확인
            boolean shouldTrigger = false;
            if (savedAlertSetting.getAlertType().name().equals("ABOVE")) {
                shouldTrigger = currentIndex >= savedAlertSetting.getThreshold();
            } else if (savedAlertSetting.getAlertType().name().equals("BELOW")) {
                shouldTrigger = currentIndex <= savedAlertSetting.getThreshold();
            }
            
            if (!shouldTrigger) {
                log.info("조건을 만족하지 않음 - 현재 인덱스: {}, 설정된 임계값: {}, 알림 타입: {}", 
                        currentIndex, savedAlertSetting.getThreshold(), savedAlertSetting.getAlertType());
                return ResponseEntity.ok("Alert condition not met. Current index: " + currentIndex + 
                        ", Threshold: " + savedAlertSetting.getThreshold() + 
                        ", Type: " + savedAlertSetting.getAlertType());
            }
            
            log.info("조건 만족 - 알림 발송 진행");
            // 알림 발송 및 메시지 내용 저장 (저장된 AlertSetting 사용)
            AlertHistory alertHistory = sendAlertWithContent(user, savedAlertSetting, currentIndex);
            
            return ResponseEntity.ok("Alert sent successfully with index: " + currentIndex);
        } catch (Exception e) {
            log.error("Alert sending failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Alert sending failed: " + e.getMessage());
        }
    }

    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail() {
        log.info("HTML 이메일 테스트 API 호출됨");
        try {
            emailService.sendSimpleTestEmail();
            return ResponseEntity.ok("Simple HTML email test sent successfully");
        } catch (Exception e) {
            log.error("HTML 이메일 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Simple HTML email test failed: " + e.getMessage());
        }
    }

    @PostMapping("/test-sms")
    public ResponseEntity<String> testSms() {
        log.info("SMS 테스트 API 호출됨");
        try {
            // 실제 Fear & Greed Index 가져오기
            CryptoFearIndexResponse fearIndexResponse = cryptoFearIndexService.getCryptoFearIndex();
            int currentIndex = Integer.parseInt(fearIndexResponse.getData().get(0).getValue());
            
            SmsRequest smsRequest = SmsRequest.createSmsRequestWithIndex(currentIndex, "01099750327");
            smsService.sendSms(smsRequest);
            return ResponseEntity.ok("SMS test sent successfully with index: " + currentIndex);
        } catch (Exception e) {
            log.error("SMS 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("SMS test failed: " + e.getMessage());
        }
    }

    // 알림을 발송하고 메시지 내용을 저장하는 메소드
    private AlertHistory sendAlertWithContent(User user, AlertSetting alertSetting, int triggeredIndex) {
        log.info("=== sendAlertWithContent 메소드 시작 ===");
        log.info("User email: {}", user.getEmail());
        log.info("AlertSetting email: {}", alertSetting.isEmail());
        log.info("AlertSetting sms: {}", alertSetting.isSms());
        log.info("Triggered index: {}", triggeredIndex);
        
        boolean emailSent = false;
        boolean smsSent = false;
        String emailContent = null;
        String smsContent = null;
        String emailSubject = null;
        
        // 사용자가 설정한 채널을 확인하여 이메일 또는 SMS 발송
        if (alertSetting.isEmail()) {
            log.info("이메일 발송 시도 중...");
            try {
                EmailRequest emailRequest = emailService.createEmailRequest(alertSetting, user.getEmail());
                log.info("EmailRequest 생성됨: {} / {}", emailRequest.getToEmail(), emailRequest.getSubject());
                emailService.sendEmail(emailRequest);
                emailSent = true;
                emailContent = emailRequest.getBody();
                emailSubject = emailRequest.getSubject();
                log.info("✅ 이메일 발송 성공!");
            } catch (Exception e) {
                log.error("❌ 이메일 발송 실패: {}", e.getMessage(), e);
                throw e;
            }
        }

        if (alertSetting.isSms()) {
            log.info("SMS 발송 시도 중...");
            try {
                SmsRequest smsRequest = SmsRequest.createSmsRequestWithIndex(triggeredIndex, user.getPhoneNumber());
                smsService.sendSms(smsRequest);
                smsSent = true;
                smsContent = smsRequest.getContent();
                log.info("✅ SMS 발송 성공!");
            } catch (Exception e) {
                log.error("❌ SMS 발송 실패: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        // AlertHistory에 메시지 내용과 함께 저장
        return alertHistoryService.createAlertHistoryWithContent(
                user, alertSetting, triggeredIndex, 
                emailSent, smsSent, false,
                emailContent, smsContent, emailSubject
        );
    }
}
