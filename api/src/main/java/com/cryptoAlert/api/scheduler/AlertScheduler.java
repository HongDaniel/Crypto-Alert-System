package com.cryptoAlert.api.scheduler;

import com.cryptoAlert.AlertHistoryService;
import com.cryptoAlert.AlertSettingService;
import com.cryptoAlert.CryptoFearIndexService;
import com.cryptoAlert.EmailService;
import com.cryptoAlert.SmsService;
import com.cryptoAlert.dto.request.EmailRequest;
import com.cryptoAlert.dto.request.SmsRequest;
import com.cryptoAlert.dto.response.CryptoFearIndexResponse;
import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertSettingService alertSettingService;
    private final CryptoFearIndexService cryptoFearIndexService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final AlertHistoryService alertHistoryService;

    /**
     * 매일 오전 9시에 실행되는 스케줄러
     * 활성화된 모든 알림 설정을 확인하고 조건에 맞는 경우 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시
    public void checkAndSendAlerts() {
        log.info("=== 자동 알림 체크 시작 ===");
        
        try {
            // 1. 현재 Fear & Greed Index 가져오기
            CryptoFearIndexResponse fearIndexResponse = cryptoFearIndexService.getCryptoFearIndex();
            int currentIndex = Integer.parseInt(fearIndexResponse.getData().get(0).getValue());
            log.info("현재 Fear & Greed Index: {}", currentIndex);
            
            // 2. 활성화된 알림 설정 조회
            List<AlertSetting> activeSettings = alertSettingService.getActiveAlertSettings();
            log.info("활성화된 알림 설정 수: {}", activeSettings.size());
            
            // 3. 각 알림 설정에 대해 조건 확인 및 알림 발송
            for (AlertSetting alertSetting : activeSettings) {
                try {
                    processAlertSetting(alertSetting, currentIndex);
                } catch (Exception e) {
                    log.error("알림 설정 처리 실패 - ID: {}, 사용자: {}, 오류: {}", 
                            alertSetting.getId(), 
                            alertSetting.getUser().getEmail(), 
                            e.getMessage(), e);
                }
            }
            
            log.info("=== 자동 알림 체크 완료 ===");
            
        } catch (Exception e) {
            log.error("자동 알림 체크 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 알림 설정 처리
     */
    private void processAlertSetting(AlertSetting alertSetting, int currentIndex) {
        User user = alertSetting.getUser();
        boolean shouldTrigger = false;
        
        // 알림 조건 확인
        switch (alertSetting.getAlertType()) {
            case ABOVE:
                shouldTrigger = currentIndex >= alertSetting.getThreshold();
                break;
            case BELOW:
                shouldTrigger = currentIndex <= alertSetting.getThreshold();
                break;
        }
        
        if (!shouldTrigger) {
            log.debug("알림 조건 미충족 - 사용자: {}, 설정 임계값: {}, 현재 지수: {}", 
                    user.getEmail(), alertSetting.getThreshold(), currentIndex);
            return;
        }
        
        log.info("알림 조건 충족 - 사용자: {}, 설정 임계값: {}, 현재 지수: {}", 
                user.getEmail(), alertSetting.getThreshold(), currentIndex);
        
        // 알림 발송
        boolean emailSent = false;
        boolean smsSent = false;
        String emailContent = null;
        String smsContent = null;
        String emailSubject = null;
        
        try {
            // 이메일 발송
            if (alertSetting.isEmail()) {
                EmailRequest emailRequest = emailService.createEmailRequest(alertSetting, user.getEmail(), currentIndex);
                emailService.sendEmail(emailRequest);
                emailSent = true;
                emailContent = emailRequest.getBody();
                emailSubject = emailRequest.getSubject();
                log.info("이메일 발송 성공 - 사용자: {}", user.getEmail());
            }
            
            // SMS 발송
            if (alertSetting.isSms()) {
                SmsRequest smsRequest = SmsRequest.createSmsRequestWithIndex(currentIndex, user.getPhoneNumber());
                smsService.sendSms(smsRequest);
                smsSent = true;
                smsContent = smsRequest.getContent();
                log.info("SMS 발송 성공 - 사용자: {}", user.getEmail());
            }
            
            // 알림 히스토리 저장
            alertHistoryService.createAlertHistoryWithContent(
                    user, alertSetting, currentIndex, 
                    emailSent, smsSent, false,
                    emailContent, smsContent, emailSubject
            );
            
        } catch (Exception e) {
            log.error("알림 발송 실패 - 사용자: {}, 오류: {}", user.getEmail(), e.getMessage(), e);
            
            // 실패한 경우에도 히스토리 저장 (발송 실패 상태로)
            alertHistoryService.createAlertHistoryWithContent(
                    user, alertSetting, currentIndex, 
                    false, false, false,
                    null, null, null
            );
        }
    }
}
