package com.cryptoAlert;

import com.cryptoAlert.dto.response.CryptoFearIndexResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CryptoFearIndexService {

    private final RestTemplate restTemplate;

    // 생성자 주입을 통해 RestTemplate 사용
    public CryptoFearIndexService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CryptoFearIndexResponse getCryptoFearIndex() {
        try {
            String url = "https://api.alternative.me/fng/?limit=1";
            return restTemplate.getForObject(url, CryptoFearIndexResponse.class);
        } catch (Exception e) {
            // API 호출 실패 시 예외 발생
            throw new RuntimeException("Fear & Greed Index API 호출 실패: " + e.getMessage(), e);
        }
    }
}
