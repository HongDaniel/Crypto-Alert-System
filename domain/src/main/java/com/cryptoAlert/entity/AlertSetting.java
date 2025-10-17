package com.cryptoAlert.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class AlertSetting {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    private int threshold;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    private boolean email;
    private boolean sms;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected AlertSetting() {
        // JPA용 기본 생성자
    }

    public AlertSetting(User user, int threshold, AlertType alertType,
                        boolean email, boolean sms) {
        this.user = user;
        this.threshold = threshold;
        this.alertType = alertType;
        this.email = email;
        this.sms = sms;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(int threshold, AlertType alertType,
                       boolean email, boolean sms) {
        this.threshold = threshold;
        this.alertType = alertType;
        this.email = email;
        this.sms = sms;
        this.updatedAt = LocalDateTime.now();
    }
}
