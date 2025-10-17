package com.cryptoAlert;

import com.cryptoAlert.config.SolapiConfig;
import com.cryptoAlert.dto.request.SmsRequest;
import com.cryptoAlert.dto.request.SolapiMessage;
import com.cryptoAlert.dto.request.SolapiSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final SolapiConfig solapiConfig;

    public void sendSms(SmsRequest smsRequest) {
        if (isPlaceholderConfig()) {
            log.info("[SMS/Solapi] 구성 값이 플레이스홀더입니다. 실제 전송을 건너뜁니다. to={}", smsRequest.getTo());
            return;
        }

        try {
            // 시뮬레이션 모드: 실제 발송 대신 로그만 출력
            log.info("[SMS/Solapi] 시뮬레이션 모드 - 실제 발송하지 않음");
            log.info("[SMS/Solapi] 발송할 메시지: to={}, content={}", smsRequest.getTo(), smsRequest.getContent());
        } catch (Exception e) {
            log.error("[SMS/Solapi] Exception details: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Solapi 전송 실패", e);
        }
    }

    public String sendMany(SolapiSendRequest request) {
        if (isPlaceholderConfig()) {
            log.info("[SMS/Solapi] 구성 값이 플레이스홀더입니다. 실제 전송을 건너뜁니다.");
            return "{\"skipped\":true}";
        }

        log.info("[SMS/Solapi] API Key: {}", solapiConfig.getApiKey());
        log.info("[SMS/Solapi] API Secret: {}...", solapiConfig.getApiSecret().substring(0, 8));
        log.info("[SMS/Solapi] From: {}", solapiConfig.getFrom());
        
        try {
            // 시뮬레이션 모드: 실제 발송 대신 로그만 출력
            log.info("[SMS/Solapi] 시뮬레이션 모드 - 실제 발송하지 않음 (다중 메시지)");
            for (SolapiMessage solapiMessage : request.getMessages()) {
                log.info("[SMS/Solapi] 발송할 메시지: to={}, content={}", solapiMessage.getTo(), solapiMessage.getText());
                if (solapiMessage.getSubject() != null && !solapiMessage.getSubject().isEmpty()) {
                    log.info("[SMS/Solapi] Subject: {}", solapiMessage.getSubject());
                }
                if (solapiMessage.getImageId() != null && !solapiMessage.getImageId().isEmpty()) {
                    log.info("[SMS/Solapi] ImageId: {}", solapiMessage.getImageId());
                }
            }
            return "{\"simulation\": true, \"message\": \"SMS 발송이 시뮬레이션 모드로 실행되었습니다.\"}";
            
        } catch (Exception e) {
            log.error("[SMS/Solapi] Exception details: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Solapi 전송 실패", e);
        }
    }


    private boolean isPlaceholderConfig() {
        return solapiConfig.getApiKey() == null || solapiConfig.getApiSecret() == null || solapiConfig.getFrom() == null ||
                solapiConfig.getApiKey().equalsIgnoreCase("YOUR_SOLAPI_API_KEY") ||
                solapiConfig.getApiSecret().equalsIgnoreCase("YOUR_SOLAPI_API_SECRET") ||
                solapiConfig.getFrom().equalsIgnoreCase("YOUR_SENDER_PHONE_NUMBER");
    }
}
