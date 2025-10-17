package com.cryptoAlert.solapi;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;

public class SolapiClient {

    public static String sendMessage(String apiKey, String apiSecret, String messageJson) throws Exception {
        String authHeader = SolapiAuth.createAuthHeader(apiKey, apiSecret);

        // SSL 검증 비활성화를 위한 TrustManager 설정
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpClient client = HttpClient.newBuilder()
                .sslContext(sc)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.solapi.com/messages/v4/send-many/detail"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(messageJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Solapi API Error: " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }
}