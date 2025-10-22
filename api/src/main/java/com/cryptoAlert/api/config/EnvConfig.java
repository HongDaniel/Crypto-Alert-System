package com.cryptoAlert.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EnvConfig {
    
    @PostConstruct
    public void loadEnvFile() {
        try {
            // 프로젝트 루트의 .env 파일을 찾기 위해 여러 경로 시도
            File envFile = null;
            String[] possiblePaths = {
                "../../.env",  // api 폴더에서 2단계 상위
                "../.env",     // api 폴더에서 1단계 상위
                ".env",        // 현재 디렉토리
                "/home/ubuntu/crypto-alert-deploy/.env",  // EC2 배포 경로
                "/Users/danny/Desktop/Crypto-Alert-multiModule/.env"  // 로컬 개발 경로
            };
            
            for (String path : possiblePaths) {
                envFile = new File(path);
                if (envFile.exists()) {
                    log.info("Found .env file at: {}", envFile.getAbsolutePath());
                    break;
                }
            }
            
            if (envFile != null && envFile.exists()) {
                log.info("Loading .env file...");
                Files.lines(envFile.toPath())
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            System.setProperty(parts[0].trim(), parts[1].trim());
                            log.info("Loaded environment variable: {}", parts[0].trim());
                        }
                    });
                log.info("Environment variables loaded successfully");
            } else {
                log.warn(".env file not found, using default values");
            }
        } catch (Exception e) {
            log.error("Failed to load .env file: {}", e.getMessage());
        }
    }
}
