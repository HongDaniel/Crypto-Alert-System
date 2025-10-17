package com.cryptoAlert.dto.request;

import com.cryptoAlert.entity.AlertType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlertSettingRequest {

    private int threshold;
    private AlertType alertType;
    private boolean email;
    private boolean sms;

    public AlertSettingRequest(int threshold, AlertType alertType,
                               boolean email, boolean sms) {
        this.threshold = threshold;
        this.alertType = alertType;
        this.email = email;
        this.sms = sms;
    }
}

