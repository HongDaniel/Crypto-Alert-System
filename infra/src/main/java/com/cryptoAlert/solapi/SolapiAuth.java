package com.cryptoAlert.solapi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public class SolapiAuth {

    public static String generateSignature(String apiSecret, String dateTime, String salt)
            throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal((dateTime + salt).getBytes());
        return HexFormat.of().formatHex(hash);
    }

    public static String createAuthHeader(String apiKey, String apiSecret) throws Exception {
        // ISO 8601 형식으로 날짜 생성 (밀리초를 000으로 설정)
        String dateTime = Instant.now().toString().replaceAll("\\.\\d+Z", ".000Z");
        String salt = UUID.randomUUID().toString().replace("-", "");
        String signature = generateSignature(apiSecret, dateTime, salt);
        return "HMAC-SHA256 apiKey=%s, date=%s, salt=%s, signature=%s".formatted(apiKey, dateTime, salt, signature);
    }
}


