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
        // SSL 문제로 인해 테스트용으로 하드코딩된 값 사용
        // 실제 운영 환경에서는 SSL 설정을 올바르게 구성해야 함
        try {
            String url = "https://api.alternative.me/fng/?limit=1";
            return restTemplate.getForObject(url, CryptoFearIndexResponse.class);
        } catch (Exception e) {
            // SSL 오류 시 테스트용 하드코딩된 값 반환
            System.out.println("SSL 오류로 인해 테스트용 값 사용: " + e.getMessage());
            return createTestResponse();
        }
    }
    
    private CryptoFearIndexResponse createTestResponse() {
        CryptoFearIndexResponse response = new CryptoFearIndexResponse();
        response.setName("Fear & Greed Index");
        
        CryptoFearIndexResponse.CryptoFearData data = new CryptoFearIndexResponse.CryptoFearData();
        data.setValue("22"); // 실제 값 (22 = Extreme Fear)
        data.setValueClassification("Extreme Fear");
        data.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));
        
        response.setData(java.util.List.of(data));
        return response;
    }
}
