package com.cryptoAlert.api.controller;

import com.cryptoAlert.CryptoFearIndexService;
import com.cryptoAlert.SmsService;
import com.cryptoAlert.dto.request.SolapiSendRequest;
import com.cryptoAlert.dto.request.SmsRequest;
import com.cryptoAlert.dto.response.CryptoFearIndexResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final SmsService smsService;
    private final CryptoFearIndexService cryptoFearIndexService;

    @Operation(summary = "단일 SMS 발송", description = "단일 SMS 메시지를 발송합니다.")
    @PostMapping("/send-single")
    public ResponseEntity<String> sendSingleSms(@RequestBody SmsRequest smsRequest) {
        try {
            smsService.sendSms(smsRequest);
            return ResponseEntity.ok("SMS sent successfully");
        } catch (Exception e) {
            log.error("단일 SMS 발송 실패: to={}, error={}", smsRequest.getTo(), e.getMessage());
            return ResponseEntity.status(500).body("SMS send failed: " + e.getMessage());
        }
    }

    @Operation(summary = "여러 메시지 발송", description = "여러 개의 메시지를 한 번에 발송합니다.")
    @PostMapping("/send-many")
    public ResponseEntity<String> sendManyMessages(@RequestBody SolapiSendRequest request) {
        try {
            String response = smsService.sendMany(request);
            return ResponseEntity.ok("Multiple messages sent successfully: " + response);
        } catch (Exception e) {
            log.error("여러 메시지 발송 실패: count={}, error={}", request.getMessages().size(), e.getMessage());
            return ResponseEntity.status(500).body("Multiple message send failed: " + e.getMessage());
        }
    }

    @Operation(summary = "SMS 테스트", description = "현재 Fear & Greed Index를 사용한 SMS 테스트를 수행합니다.")
    @PostMapping("/test")
    public ResponseEntity<String> testSms() {
        try {
            // 실제 Fear & Greed Index 가져오기
            CryptoFearIndexResponse fearIndexResponse = cryptoFearIndexService.getCryptoFearIndex();
            int currentIndex = Integer.parseInt(fearIndexResponse.getData().get(0).getValue());
            
            // 기본 사용자 정보로 SMS 테스트 (실제 운영에서는 인증된 사용자 사용)
            SmsRequest smsRequest = SmsRequest.createSmsRequestWithIndex(currentIndex, "01000000000");
            smsService.sendSms(smsRequest);
            return ResponseEntity.ok("SMS test successful with index: " + currentIndex);
        } catch (Exception e) {
            log.error("SMS 테스트 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(500).body("SMS test failed: " + e.getMessage());
        }
    }
}