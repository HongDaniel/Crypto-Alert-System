package com.cryptoAlert.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolapiMessage {
    private String to;
    private String from;
    private String text;
    private String subject; // LMS/MMS 선택 시 사용
    private String imageId; // MMS 선택 시 사용
}


