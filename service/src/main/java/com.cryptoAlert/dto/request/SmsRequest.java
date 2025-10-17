package com.cryptoAlert.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    private String to;       // 수신자 전화번호
    private String content;  // SMS 내용

    // 인덱스 값을 받아서 메시지를 생성하는 메서드
    public static SmsRequest createSmsRequestWithIndex(int indexValue, String to) {
        String content = generateSmsContentByIndex(indexValue);
        return new SmsRequest(to, content);
    }
    
    private static String generateSmsContentByIndex(int indexValue) {
        String baseMessage = "[Crypto Alert] Fear & Greed Index : " + indexValue;
        
        if (indexValue >= 80) {
            return baseMessage + "\n사람들이 엄청난 탐욕을 부리고 있어! 적극적으로 매도해!";
        } else if (indexValue >= 60) {
            return baseMessage + "\n사람들이 탐욕을 부리고 있어. 분할로 매도해.";
        } else if (indexValue >= 40) {
            return baseMessage + "\nHODL!";
        } else if (indexValue >= 20) {
            return baseMessage + "\n사람들이 무서워하고 있어. 분할로 매수해.";
        } else {
            return baseMessage + "\n사람들이 지금 공포에 떨고 있어! 적극적으로 매수해!";
        }
    }
}
