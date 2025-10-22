package com.cryptoAlert.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    @PostConstruct
    public void disableSSLVerification() {
        try {
            // 모든 SSL 인증서를 신뢰하는 TrustManager 생성
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // 모든 클라이언트 인증서 신뢰
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // 모든 서버 인증서 신뢰
                    }
                }
            };

            // SSL 컨텍스트 생성 및 TrustManager 설정
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // HttpsURLConnection의 기본 SSL 컨텍스트 설정
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
            // JVM 시스템 프로퍼티로 SSL 인증서 검증 비활성화
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("trust_all_cert", "true");
            
        } catch (Exception e) {
            throw new RuntimeException("SSL 설정 중 오류 발생", e);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
