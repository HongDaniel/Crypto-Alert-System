# 데이터베이스 설정 가이드

## 🗄️ **RDS MySQL + DBeaver 연동 완료 후 설정**

### **1단계: DBeaver에서 DDL 실행**

#### 1.1 간단한 DDL 실행 (권장)
```sql
-- simple_ddl.sql 파일의 내용을 DBeaver에서 실행
-- 또는 아래 명령어로 파일 실행
SOURCE /path/to/simple_ddl.sql;
```

#### 1.2 전체 DDL 실행 (고급)
```sql
-- database_schema.sql 파일의 내용을 DBeaver에서 실행
-- 인덱스, 뷰, 파티셔닝 등 포함
```

### **2단계: 환경변수 설정**

#### 2.1 로컬 개발 환경
```bash
# .env 파일 생성
cp env.example .env

# .env 파일 수정
nano .env
```

#### 2.2 .env 파일 내용
```bash
# RDS MySQL 연결 정보
DATABASE_URL=jdbc:mysql://your-rds-endpoint.ap-northeast-2.rds.amazonaws.com:3306/crypto_alert?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DATABASE_USERNAME=admin
DATABASE_PASSWORD=your-actual-password

# Gmail 설정
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# Solapi SMS 설정
SOLAPI_API_KEY=your-solapi-api-key
SOLAPI_API_SECRET=your-solapi-api-secret
SOLAPI_FROM=01000000000

# 기본 사용자 설정
DEFAULT_USER_EMAIL=admin@example.com
DEFAULT_USER_PHONE=01000000000
DEFAULT_USERNAME=admin
DEFAULT_PASSWORD=admin
```

### **3단계: 애플리케이션 테스트**

#### 3.1 로컬에서 MySQL 연결 테스트
```bash
# 서버 재시작
pkill -f "gradle.*bootRun"
./gradlew bootRun
```

#### 3.2 데이터베이스 연결 확인
- DBeaver에서 테이블 생성 확인
- Spring Boot 로그에서 MySQL 연결 확인
- H2 콘솔 대신 DBeaver 사용

### **4단계: 데이터 마이그레이션 (필요시)**

#### 4.1 기존 H2 데이터가 있는 경우
```sql
-- DBeaver에서 실행할 마이그레이션 스크립트
-- 1. 사용자 데이터 마이그레이션
INSERT INTO app_user (email, password, phone_number, username)
SELECT email, password, phone_number, username FROM h2_user;

-- 2. 알림 설정 마이그레이션
INSERT INTO alert_setting (user_id, alert_type, threshold, email, sms)
SELECT user_id, alert_type, threshold, email, sms FROM h2_alert_setting;
```

### **5단계: 프로덕션 배포 준비**

#### 5.1 RDS 보안 그룹 설정
- **인바운드 규칙**:
  - Type: MySQL/Aurora
  - Port: 3306
  - Source: EC2 보안 그룹 ID

#### 5.2 EC2에서 환경변수 설정
```bash
# EC2에서 실행
export DATABASE_URL="jdbc:mysql://your-rds-endpoint:3306/crypto_alert?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DATABASE_USERNAME="admin"
export DATABASE_PASSWORD="your-actual-password"
# ... 기타 환경변수들
```

### **6단계: 모니터링 설정**

#### 6.1 DBeaver에서 모니터링 쿼리
```sql
-- 테이블 크기 확인
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'crypto_alert'
ORDER BY (data_length + index_length) DESC;

-- 사용자별 알림 설정 수
SELECT 
    u.email,
    COUNT(a.id) as alert_count
FROM app_user u
LEFT JOIN alert_setting a ON u.id = a.user_id
GROUP BY u.id, u.email;

-- 최근 알림 히스토리
SELECT 
    u.email,
    ah.triggered_index,
    ah.triggered_at,
    ah.sent_email,
    ah.sent_sms
FROM alert_history ah
JOIN app_user u ON ah.user_id = u.id
ORDER BY ah.triggered_at DESC
LIMIT 10;
```

### **7단계: 백업 설정**

#### 7.1 RDS 자동 백업 설정
- **백업 보존 기간**: 7일 (프리 티어)
- **백업 윈도우**: 유지보수 시간 설정
- **스냅샷**: 수동 생성 가능

#### 7.2 데이터 내보내기
```bash
# DBeaver에서 실행하거나 mysqldump 사용
mysqldump -h your-rds-endpoint -u admin -p crypto_alert > backup.sql
```

## ✅ **완료 체크리스트**

- [ ] DBeaver에서 DDL 실행 완료
- [ ] 테이블 생성 확인
- [ ] 환경변수 설정 완료
- [ ] 로컬에서 MySQL 연결 테스트
- [ ] RDS 보안 그룹 설정
- [ ] EC2 배포 준비 완료

## 🚨 **주의사항**

1. **비밀번호**: 실제 비밀번호로 변경 필수
2. **보안**: RDS는 프라이빗 서브넷에 배치 권장
3. **백업**: 정기적인 백업 설정
4. **모니터링**: CloudWatch로 성능 모니터링
5. **비용**: RDS 인스턴스 크기 조정 고려

## 📞 **문제 해결**

### 연결 오류 시
1. RDS 엔드포인트 확인
2. 보안 그룹 설정 확인
3. VPC 설정 확인
4. 방화벽 설정 확인

### 성능 이슈 시
1. 인덱스 추가
2. 쿼리 최적화
3. RDS 인스턴스 크기 조정
4. 연결 풀 설정 조정
