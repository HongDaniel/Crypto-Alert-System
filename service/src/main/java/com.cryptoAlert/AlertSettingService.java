package com.cryptoAlert;

import com.cryptoAlert.dto.request.AlertSettingRequest;
import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.AlertType;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.repository.AlertSettingRepository;
import com.cryptoAlert.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertSettingService {

    private final AlertSettingRepository alertSettingRepository;
    private final UserRepository userRepository;

    public AlertSetting createAlertSetting(AlertSettingRequest request,Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 중복 체크: 같은 사용자가 동일한 threshold와 alertType을 가진 설정이 있는지 확인
        Optional<AlertSetting> existingAlert = alertSettingRepository.findByUserIdAndThresholdAndAlertType(
                userId, request.getThreshold(), request.getAlertType());
        
        if (existingAlert.isPresent()) {
            throw new IllegalArgumentException("이미 동일한 조건의 알림 설정이 존재합니다. " +
                    "Threshold: " + request.getThreshold() + 
                    ", Type: " + request.getAlertType());
        }

        AlertSetting setting = new AlertSetting(
                user,
                request.getThreshold(),
                request.getAlertType(),
                request.isEmail(),
                request.isSms()
        );

        return alertSettingRepository.save(setting);
    }

    public AlertSetting saveAlertSetting(AlertSetting alertSetting) {
        return alertSettingRepository.save(alertSetting);
    }

    public Optional<AlertSetting> findByUserIdAndThresholdAndAlertType(Long userId, int threshold, AlertType alertType) {
        return alertSettingRepository.findByUserIdAndThresholdAndAlertType(userId, threshold, alertType);
    }

    public List<AlertSetting> getAllAlertSettings() {
        return alertSettingRepository.findAll();
    }

    public Optional<AlertSetting> getAlertSettingById(Long id) {
        return alertSettingRepository.findById(id);
    }

    public AlertSetting updateAlertSetting(Long id, AlertSettingRequest request) {
        AlertSetting alertSetting = alertSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AlertSetting not found with ID: " + id));

        alertSetting.update(
                request.getThreshold(),
                request.getAlertType(),
                request.isEmail(),
                request.isSms()
        );

        return alertSettingRepository.save(alertSetting);
    }


    public void deleteAlertSetting(Long id) {
        alertSettingRepository.deleteById(id);
    }

    /**
     * 활성화된 알림 설정 조회
     */
    public List<AlertSetting> getActiveAlertSettings() {
        return alertSettingRepository.findActiveAlertSettings();
    }
}
