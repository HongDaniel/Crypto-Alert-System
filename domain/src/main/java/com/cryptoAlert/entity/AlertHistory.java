package com.cryptoAlert.entity;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistory {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_setting_fk")
    @JsonIgnore
    private AlertSetting alertSetting; // 어떤 조건으로 발동됐는지

    @Column(name = "user_id")
    private Long userId; // 사용자 ID (JSON 직렬화용)

    private int triggeredIndex; // 발동된 지수
    private LocalDateTime triggeredAt;

    private boolean sentEmail;
    private boolean sentSms;
    private boolean sentPush;
    
    // 발송된 메시지 내용 저장
    @Column(columnDefinition = "TEXT")
    private String emailContent; // 이메일 내용
    
    @Column(columnDefinition = "TEXT")
    private String smsContent; // SMS 내용
    
    private String emailSubject; // 이메일 제목
}

