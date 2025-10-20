package com.cryptoAlert.repository;

import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertSettingRepository extends JpaRepository<AlertSetting,Long> {
    
    // 활성화된 알림 설정 조회 (이메일 또는 SMS가 활성화된 것)
    @Query("SELECT alertSetting FROM AlertSetting alertSetting WHERE alertSetting.email = true OR alertSetting.sms = true")
    List<AlertSetting> findActiveAlertSettings();
    
    // 사용자별 중복 체크: 같은 사용자가 동일한 threshold와 alertType을 가진 설정이 있는지 확인
    @Query("SELECT alertSetting FROM AlertSetting alertSetting WHERE alertSetting.user.id = :userId AND alertSetting.threshold = :threshold AND alertSetting.alertType = :alertType")
    Optional<AlertSetting> findByUserIdAndThresholdAndAlertType(@Param("userId") Long userId, @Param("threshold") int threshold, @Param("alertType") AlertType alertType);
    
    // 특정 사용자의 모든 알림 설정 조회
    @Query("SELECT alertSetting FROM AlertSetting alertSetting WHERE alertSetting.user.id = :userId")
    List<AlertSetting> findByUserId(@Param("userId") Long userId);
}
