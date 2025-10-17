package com.cryptoAlert;

import com.cryptoAlert.entity.AlertHistory;
import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.repository.AlertHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertHistoryService {

    private final AlertHistoryRepository alertHistoryRepository;

    /**
     * 알림 히스토리 생성
     */
    @Transactional
    public AlertHistory createAlertHistory(User user, AlertSetting alertSetting, int triggeredIndex, 
                                         boolean sentEmail, boolean sentSms, boolean sentPush) {
        AlertHistory alertHistory = new AlertHistory();
        alertHistory.setUser(user);
        alertHistory.setUserId(user.getId()); // userId 설정
        alertHistory.setAlertSetting(alertSetting);
        alertHistory.setTriggeredIndex(triggeredIndex);
        alertHistory.setTriggeredAt(LocalDateTime.now());
        alertHistory.setSentEmail(sentEmail);
        alertHistory.setSentSms(sentSms);
        alertHistory.setSentPush(sentPush);

        AlertHistory savedHistory = alertHistoryRepository.save(alertHistory);
        log.info("Alert history created: userId={}, triggeredIndex={}, sentEmail={}, sentSms={}", 
                user.getId(), triggeredIndex, sentEmail, sentSms);
        
        return savedHistory;
    }

    /**
     * 메시지 내용과 함께 알림 히스토리 생성
     */
    @Transactional
    public AlertHistory createAlertHistoryWithContent(User user, AlertSetting alertSetting, int triggeredIndex, 
                                                    boolean sentEmail, boolean sentSms, boolean sentPush,
                                                    String emailContent, String smsContent, String emailSubject) {
        AlertHistory alertHistory = new AlertHistory();
        alertHistory.setUser(user);
        alertHistory.setUserId(user.getId()); // userId 설정
        alertHistory.setAlertSetting(alertSetting);
        alertHistory.setTriggeredIndex(triggeredIndex);
        alertHistory.setTriggeredAt(LocalDateTime.now());
        alertHistory.setSentEmail(sentEmail);
        alertHistory.setSentSms(sentSms);
        alertHistory.setSentPush(sentPush);
        alertHistory.setEmailContent(emailContent);
        alertHistory.setSmsContent(smsContent);
        alertHistory.setEmailSubject(emailSubject);

        AlertHistory savedHistory = alertHistoryRepository.save(alertHistory);
        log.info("Alert history with content created: userId={}, triggeredIndex={}, sentEmail={}, sentSms={}", 
                user.getId(), triggeredIndex, sentEmail, sentSms);
        
        return savedHistory;
    }

    /**
     * 알림 히스토리 상세 조회
     */
    public AlertHistory getAlertHistoryById(Long historyId) {
        return alertHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Alert history not found: " + historyId));
    }

    /**
     * 사용자별 알림 히스토리 조회 (최신순)
     */
    public List<AlertHistory> getAlertHistoryByUser(User user) {
        return alertHistoryRepository.findByUserOrderByTriggeredAtDesc(user);
    }

    /**
     * 사용자 ID로 알림 히스토리 조회 (최신순)
     */
    public List<AlertHistory> getAlertHistoryByUserId(Long userId) {
        return alertHistoryRepository.findByUserIdOrderByTriggeredAtDesc(userId);
    }

    /**
     * 최근 10개 알림 히스토리 조회
     */
    public List<AlertHistory> getRecentAlertHistory(Long userId) {
        List<AlertHistory> allHistory = alertHistoryRepository.findByUserIdOrderByTriggeredAtDesc(userId);
        return allHistory.stream()
                .limit(10)
                .toList();
    }

    /**
     * 알림 히스토리 삭제
     */
    @Transactional
    public void deleteAlertHistory(Long historyId) {
        alertHistoryRepository.deleteById(historyId);
        log.info("Alert history deleted: historyId={}", historyId);
    }
}
