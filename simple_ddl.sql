-- Crypto Alert System - 간단한 DDL
-- DBeaver에서 실행할 수 있는 최소한의 DDL

-- 1. 사용자 테이블
CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

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
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

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
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (user_fk) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (alert_setting_fk) REFERENCES alert_setting(id) ON DELETE CASCADE
);

-- 4. 공포&탐욕 지수 히스토리 테이블
CREATE TABLE fear_greed_index_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    index_value INT NOT NULL,
    classification VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

-- 5. Hibernate 시퀀스 테이블
CREATE TABLE hibernate_sequence (
    next_val BIGINT
);

-- 시퀀스 초기값
INSERT INTO hibernate_sequence (next_val) VALUES (1);

-- 6. 기본 데이터 삽입
-- 기본 사용자 (비밀번호: admin)
INSERT INTO app_user (id, email, password, phone_number, username) VALUES 
(1, 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '01000000000', 'admin');

-- 기본 알림 설정
INSERT INTO alert_setting (id, user_id, alert_type, threshold, email, sms) VALUES 
(1, 1, 'ABOVE', 70, TRUE, TRUE);

-- 완료!
SELECT 'Database setup completed!' as status;
