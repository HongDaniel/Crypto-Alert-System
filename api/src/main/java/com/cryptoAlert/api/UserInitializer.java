package com.cryptoAlert.api;

import com.cryptoAlert.AlertSettingService;
import com.cryptoAlert.UserService;
import com.cryptoAlert.dto.request.AlertSettingRequest;
import com.cryptoAlert.dto.request.UserRequest;
import com.cryptoAlert.entity.AlertType;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserInitializer {

    private final UserService userService;
    private final AlertSettingService alertSettingService;
    private final UserRepository userRepository;

    @Value("${app.default-user.email:test@example.com}")
    private String defaultEmail;

    @Value("${app.default-user.phone:01000000000}")
    private String defaultPhoneNumber;

    @Value("${app.default-user.username:test}")
    private String defaultUsername;

    @Value("${app.default-user.password:test}")
    private String defaultPassword;

    @Value("${app.default-alert.threshold:70}")
    private int defaultThreshold;

    @PostConstruct
    public void initDefaultData() {
        log.info("기본 사용자 데이터 초기화 시작");

        try {
            // 1. 기본 사용자 생성
            userService.create(new UserRequest(defaultUsername, defaultPassword, defaultEmail, defaultPhoneNumber));
            log.info("기본 사용자 생성됨: {}", defaultEmail);
            
            // 2. 기본 알림 설정 생성
            User user = userRepository.findByEmail(defaultEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + defaultEmail));
            
            // 기본 알림 설정 생성
            AlertSettingRequest alertRequest = new AlertSettingRequest(defaultThreshold, AlertType.ABOVE, true, true);
            alertSettingService.createAlertSetting(alertRequest, user.getId());
            log.info("기본 알림 설정 생성됨: threshold={}, ABOVE, email=true, sms=true", defaultThreshold);
            
        } catch (IllegalStateException e) {
            log.info("기본 사용자 이미 존재: {}", defaultEmail);
        } catch (Exception e) {
            log.error("초기 데이터 생성 실패: {}", e.getMessage(), e);
        }
    }
}

