-- Crypto Alert System Database Schema
-- MySQL 8.0+ 호환

-- 데이터베이스 생성 (이미 생성되어 있다면 생략)
-- CREATE DATABASE crypto_alert CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE crypto_alert;

-- 1. 사용자 테이블
CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 알림 설정 테이블
CREATE TABLE alert_setting (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(255) NOT NULL,
    threshold INT NOT NULL,
    email BOOLEAN NOT NULL DEFAULT FALSE,
    sms BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_threshold (threshold),
    UNIQUE KEY uk_user_threshold_type (user_id, threshold, alert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 알림 히스토리 테이블
CREATE TABLE alert_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    user_fk BIGINT NOT NULL,
    alert_setting_fk BIGINT NOT NULL,
    triggered_index INT NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    sent_email BOOLEAN NOT NULL DEFAULT FALSE,
    sent_sms BOOLEAN NOT NULL DEFAULT FALSE,
    sent_push BOOLEAN NOT NULL DEFAULT FALSE,
    email_subject VARCHAR(255),
    email_content TEXT,
    sms_content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (user_fk) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (alert_setting_fk) REFERENCES alert_setting(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_triggered_at (triggered_at),
    INDEX idx_alert_setting_fk (alert_setting_fk)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 공포&탐욕 지수 히스토리 테이블
CREATE TABLE fear_greed_index_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    index_value INT NOT NULL,
    classification VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_index_value (index_value),
    INDEX idx_classification (classification)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 시퀀스 테이블 (Hibernate 시퀀스용)
CREATE TABLE hibernate_sequence (
    next_val BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 시퀀스 초기값 설정
INSERT INTO hibernate_sequence (next_val) VALUES (1);

-- 6. 기본 데이터 삽입
-- 기본 사용자 생성
INSERT INTO app_user (id, email, password, phone_number, username) VALUES 
(1, 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '01000000000', 'admin');

-- 기본 알림 설정 생성
INSERT INTO alert_setting (id, user_id, alert_type, threshold, email, sms) VALUES 
(1, 1, 'ABOVE', 70, TRUE, TRUE);

-- 7. 뷰 생성 (선택사항)
-- 사용자별 알림 설정 요약 뷰
CREATE VIEW user_alert_summary AS
SELECT 
    u.id as user_id,
    u.email,
    u.username,
    COUNT(a.id) as total_alerts,
    SUM(CASE WHEN a.alert_type = 'ABOVE' THEN 1 ELSE 0 END) as above_alerts,
    SUM(CASE WHEN a.alert_type = 'BELOW' THEN 1 ELSE 0 END) as below_alerts,
    SUM(CASE WHEN a.email = TRUE THEN 1 ELSE 0 END) as email_enabled_alerts,
    SUM(CASE WHEN a.sms = TRUE THEN 1 ELSE 0 END) as sms_enabled_alerts
FROM app_user u
LEFT JOIN alert_setting a ON u.id = a.user_id
GROUP BY u.id, u.email, u.username;

-- 최근 알림 히스토리 뷰
CREATE VIEW recent_alert_history AS
SELECT 
    ah.id,
    u.email,
    u.username,
    aset.alert_type,
    aset.threshold,
    ah.triggered_index,
    ah.triggered_at,
    ah.sent_email,
    ah.sent_sms,
    ah.sent_push
FROM alert_history ah
JOIN app_user u ON ah.user_id = u.id
JOIN alert_setting aset ON ah.alert_setting_fk = aset.id
ORDER BY ah.triggered_at DESC;

-- 8. 인덱스 최적화
-- 복합 인덱스 추가
CREATE INDEX idx_alert_history_user_triggered ON alert_history(user_id, triggered_at DESC);
CREATE INDEX idx_alert_setting_user_active ON alert_setting(user_id, email, sms);

-- 9. 파티셔닝 (선택사항 - 대용량 데이터용)
-- 날짜별 파티셔닝 (월별)
-- ALTER TABLE alert_history PARTITION BY RANGE (YEAR(triggered_at) * 100 + MONTH(triggered_at)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     PARTITION p202404 VALUES LESS THAN (202405),
--     PARTITION p202405 VALUES LESS THAN (202406),
--     PARTITION p202406 VALUES LESS THAN (202407),
--     PARTITION p202407 VALUES LESS THAN (202408),
--     PARTITION p202408 VALUES LESS THAN (202409),
--     PARTITION p202409 VALUES LESS THAN (202410),
--     PARTITION p202410 VALUES LESS THAN (202411),
--     PARTITION p202411 VALUES LESS THAN (202412),
--     PARTITION p202412 VALUES LESS THAN (202501),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 10. 권한 설정 (필요시)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON crypto_alert.* TO 'crypto_alert_user'@'%';
-- GRANT EXECUTE ON crypto_alert.* TO 'crypto_alert_user'@'%';

-- 11. 테이블 통계 업데이트
ANALYZE TABLE app_user;
ANALYZE TABLE alert_setting;
ANALYZE TABLE alert_history;
ANALYZE TABLE fear_greed_index_history;

-- 12. 데이터베이스 설정 최적화
-- SET GLOBAL innodb_buffer_pool_size = 128M;
-- SET GLOBAL max_connections = 100;
-- SET GLOBAL query_cache_size = 32M;
-- SET GLOBAL query_cache_type = 1;

-- 완료 메시지
SELECT 'Crypto Alert System Database Schema 생성 완료!' as message;
