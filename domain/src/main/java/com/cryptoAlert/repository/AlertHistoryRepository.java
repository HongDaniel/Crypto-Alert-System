package com.cryptoAlert.repository;

import com.cryptoAlert.entity.AlertHistory;
import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    
    // 사용자별 알림 히스토리 조회 (최신순)
    List<AlertHistory> findByUserOrderByTriggeredAtDesc(User user);
    
    // 사용자 ID로 알림 히스토리 조회 (최신순)
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.user.id = :userId ORDER BY ah.triggeredAt DESC")
    List<AlertHistory> findByUserIdOrderByTriggeredAtDesc(@Param("userId") Long userId);
    
    // 최근 N개 알림 히스토리 조회
    @Query("SELECT ah FROM AlertHistory ah WHERE ah.user.id = :userId ORDER BY ah.triggeredAt DESC")
    List<AlertHistory> findTop10ByUserIdOrderByTriggeredAtDesc(@Param("userId") Long userId);
    
    // 활성화된 알림 설정 조회 (이메일 또는 SMS가 활성화된 것)
    @Query("SELECT DISTINCT alertSetting FROM AlertSetting alertSetting WHERE alertSetting.email = true OR alertSetting.sms = true")
    List<AlertSetting> findActiveAlertSettings();
}
