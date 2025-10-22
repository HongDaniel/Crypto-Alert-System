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

import java.util.List;
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

    @Operation(summary = "모든 Active Alert 발송", description = "현재 로그인한 사용자의 모든 Active Alert를 확인하고 조건을 만족하는 알림을 발송합니다")
    @PostMapping("/send")
    public ResponseEntity<String> sendAlert(@AuthenticationPrincipal User user) {
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
        log.info("Alert Send API 호출됨 - User: {}", user.getEmail());
        
        try {
            // 현재 Fear & Greed Index 가져오기
            CryptoFearIndexResponse fearIndexResponse = cryptoFearIndexService.getCryptoFearIndex();
            int currentIndex = Integer.parseInt(fearIndexResponse.getData().get(0).getValue());
            log.info("현재 Fear & Greed Index: {}", currentIndex);
            
            // 현재 사용자의 모든 Active Alert 설정 조회
            List<AlertSetting> userActiveAlerts = alertSettingService.getActiveAlertSettingsByUserId(user.getId());
            log.info("사용자 {}의 총 {}개의 Active Alert 설정을 찾았습니다", user.getEmail(), userActiveAlerts.size());
            
            int triggeredCount = 0;
            int totalCount = userActiveAlerts.size();
            
            for (AlertSetting alertSetting : userActiveAlerts) {
                try {
                    // 조건 확인
                    boolean shouldTrigger = false;
                    if (alertSetting.getAlertType().name().equals("ABOVE")) {
                        shouldTrigger = currentIndex >= alertSetting.getThreshold();
                    } else if (alertSetting.getAlertType().name().equals("BELOW")) {
                        shouldTrigger = currentIndex <= alertSetting.getThreshold();
                    }
                    
                    if (shouldTrigger) {
                        log.info("조건 만족 - Alert ID: {}, 임계값: {}, 타입: {}, 현재 인덱스: {}", 
                                alertSetting.getId(), alertSetting.getThreshold(), 
                                alertSetting.getAlertType(), currentIndex);
                        
                        // 알림 발송
                        AlertHistory alertHistory = sendAlertWithContent(user, alertSetting, currentIndex);
                        triggeredCount++;
                        
                        log.info("Alert 발송 완료 - ID: {}, 사용자: {}", alertSetting.getId(), user.getEmail());
                    } else {
                        log.info("조건 불만족 - Alert ID: {}, 임계값: {}, 타입: {}, 현재 인덱스: {}", 
                                alertSetting.getId(), alertSetting.getThreshold(), 
                                alertSetting.getAlertType(), currentIndex);
                    }
                } catch (Exception e) {
                    log.error("Alert ID {} 발송 실패: {}", alertSetting.getId(), e.getMessage(), e);
                }
            }
            
            String result = String.format("수동 발송 완료 - 총 %d개 중 %d개 발송됨 (현재 인덱스: %d)", 
                                        totalCount, triggeredCount, currentIndex);
            log.info(result);
            return ResponseEntity.ok(result);
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
            
            // 기본 사용자 정보로 SMS 테스트 (실제 운영에서는 인증된 사용자 사용)
            SmsRequest smsRequest = SmsRequest.createSmsRequestWithIndex(currentIndex, "01000000000");
            smsService.sendSms(smsRequest);
            return ResponseEntity.ok("SMS test sent successfully with index: " + currentIndex);
        } catch (Exception e) {
            log.error("SMS 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("SMS test failed: " + e.getMessage());
        }
    }

    @Operation(summary = "모든 Active Alert 수동 발송", description = "모든 활성화된 알림 설정을 확인하고 조건을 만족하는 알림을 발송합니다")
    @PostMapping("/send-all")
    public ResponseEntity<String> sendAllActiveAlerts() {
        log.info("모든 Active Alert 수동 발송 API 호출됨");
        
        // 기본 사용자 사용 (인증 없이)
        User user;
        try {
            user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("기본 사용자를 찾을 수 없습니다. ID: 1"));
            log.info("기본 사용자 사용: {}", user.getEmail());
        } catch (Exception e) {
            log.error("기본 사용자 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Default user not found");
        }
        
        try {
            // 현재 Fear & Greed Index 가져오기
            CryptoFearIndexResponse fearIndexResponse = cryptoFearIndexService.getCryptoFearIndex();
            int currentIndex = Integer.parseInt(fearIndexResponse.getData().get(0).getValue());
            log.info("현재 Fear & Greed Index: {}", currentIndex);
            
            // 모든 Active Alert 설정 조회
            List<AlertSetting> activeAlerts = alertSettingService.getActiveAlertSettings();
            log.info("총 {}개의 Active Alert 설정을 찾았습니다", activeAlerts.size());
            
            int triggeredCount = 0;
            int totalCount = activeAlerts.size();
            
            for (AlertSetting alertSetting : activeAlerts) {
                try {
                    // 조건 확인
                    boolean shouldTrigger = false;
                    if (alertSetting.getAlertType().name().equals("ABOVE")) {
                        shouldTrigger = currentIndex >= alertSetting.getThreshold();
                    } else if (alertSetting.getAlertType().name().equals("BELOW")) {
                        shouldTrigger = currentIndex <= alertSetting.getThreshold();
                    }
                    
                    if (shouldTrigger) {
                        log.info("조건 만족 - Alert ID: {}, 임계값: {}, 타입: {}, 현재 인덱스: {}", 
                                alertSetting.getId(), alertSetting.getThreshold(), 
                                alertSetting.getAlertType(), currentIndex);
                        
                        // 해당 AlertSetting의 사용자 정보 조회
                        User alertUser = userRepository.findById(alertSetting.getUser().getId())
                            .orElseThrow(() -> new RuntimeException("AlertSetting의 사용자를 찾을 수 없습니다. ID: " + alertSetting.getUser().getId()));
                        
                        // 알림 발송
                        AlertHistory alertHistory = sendAlertWithContent(alertUser, alertSetting, currentIndex);
                        triggeredCount++;
                        
                        log.info("Alert 발송 완료 - ID: {}, 사용자: {}", alertSetting.getId(), alertUser.getEmail());
                    } else {
                        log.info("조건 불만족 - Alert ID: {}, 임계값: {}, 타입: {}, 현재 인덱스: {}", 
                                alertSetting.getId(), alertSetting.getThreshold(), 
                                alertSetting.getAlertType(), currentIndex);
                    }
                } catch (Exception e) {
                    log.error("Alert ID {} 발송 실패: {}", alertSetting.getId(), e.getMessage(), e);
                }
            }
            
            String result = String.format("수동 발송 완료 - 총 %d개 중 %d개 발송됨 (현재 인덱스: %d)", 
                                        totalCount, triggeredCount, currentIndex);
            log.info(result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("모든 Active Alert 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Send all alerts failed: " + e.getMessage());
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
                EmailRequest emailRequest = emailService.createEmailRequest(alertSetting, user.getEmail(), triggeredIndex);
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
