# AWS EC2 + RDS 배포 가이드

## 🚀 **1단계: RDS MySQL 데이터베이스 생성**

### 1.1 RDS 인스턴스 생성
1. AWS 콘솔 → RDS → 데이터베이스 생성
2. **엔진 유형**: MySQL
3. **템플릿**: 프리 티어 (개발/테스트용)
4. **DB 인스턴스 식별자**: `crypto-alert-db`
5. **마스터 사용자명**: `admin`
6. **마스터 암호**: 안전한 비밀번호 설정
7. **DB 인스턴스 클래스**: `db.t3.micro`
8. **스토리지**: 20GB (프리 티어)
9. **VPC**: 기본 VPC 선택
10. **퍼블릭 액세스**: 예 (EC2에서 접근 가능하도록)
11. **VPC 보안 그룹**: 새로 생성
12. **데이터베이스 포트**: 3306

### 1.2 보안 그룹 설정
- **인바운드 규칙**:
  - Type: MySQL/Aurora
  - Port: 3306
  - Source: EC2 보안 그룹 ID (나중에 설정)

## 🖥️ **2단계: EC2 인스턴스 생성**

### 2.1 EC2 인스턴스 생성
1. AWS 콘솔 → EC2 → 인스턴스 시작
2. **AMI**: Ubuntu Server 22.04 LTS
3. **인스턴스 유형**: t2.micro (프리 티어)
4. **키 페어**: 새로 생성 또는 기존 사용
5. **보안 그룹**: 새로 생성
   - SSH (22): My IP
   - HTTP (80): Anywhere (0.0.0.0/0)
   - HTTPS (443): Anywhere (0.0.0.0/0)
   - Custom TCP (8080): Anywhere (0.0.0.0/0)

### 2.2 EC2 연결 및 환경 설정
```bash
# EC2에 SSH 연결
ssh -i your-key.pem ubuntu@your-ec2-ip

# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# Java 17 설치
sudo apt install openjdk-17-jdk -y

# Nginx 설치
sudo apt install nginx -y

# MySQL 클라이언트 설치 (선택사항)
sudo apt install mysql-client -y
```

## 📦 **3단계: 애플리케이션 배포**

### 3.1 로컬에서 배포 파일 생성
```bash
# 프로젝트 루트에서 실행
./deploy.sh
```

### 3.2 EC2에 파일 업로드
```bash
# SCP로 파일 업로드
scp -i your-key.pem -r deploy/ ubuntu@your-ec2-ip:/home/ubuntu/
scp -i your-key.pem crypto-alert.service ubuntu@your-ec2-ip:/home/ubuntu/
scp -i your-key.pem nginx.conf ubuntu@your-ec2-ip:/home/ubuntu/
```

### 3.3 EC2에서 애플리케이션 설정
```bash
# EC2에 SSH 연결 후
cd /home/ubuntu

# 애플리케이션 디렉토리 생성
sudo mkdir -p /home/ubuntu/crypto-alert
sudo cp -r deploy/* /home/ubuntu/crypto-alert/
sudo chown -R ubuntu:ubuntu /home/ubuntu/crypto-alert

# 환경변수 파일 설정
cd /home/ubuntu/crypto-alert
cp env.example .env
nano .env  # 실제 값으로 수정

# 시스템 서비스 설정
sudo cp crypto-alert.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable crypto-alert
sudo systemctl start crypto-alert

# Nginx 설정
sudo cp nginx.conf /etc/nginx/sites-available/crypto-alert
sudo ln -s /etc/nginx/sites-available/crypto-alert /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

## 🔧 **4단계: 환경변수 설정**

### 4.1 .env 파일 수정
```bash
# RDS 엔드포인트로 변경
DATABASE_URL=jdbc:mysql://your-rds-endpoint.region.rds.amazonaws.com:3306/crypto_alert?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DATABASE_USERNAME=admin
DATABASE_PASSWORD=your-actual-password

# 실제 Gmail 정보
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# 실제 Solapi 정보
SOLAPI_API_KEY=your-actual-api-key
SOLAPI_API_SECRET=your-actual-api-secret
SOLAPI_FROM=01000000000
```

## 🚀 **5단계: 애플리케이션 시작**

### 5.1 서비스 상태 확인
```bash
# 애플리케이션 로그 확인
sudo journalctl -u crypto-alert -f

# 서비스 상태 확인
sudo systemctl status crypto-alert

# Nginx 상태 확인
sudo systemctl status nginx
```

### 5.2 접속 테스트
- **웹 애플리케이션**: `http://your-ec2-ip`
- **API 문서**: `http://your-ec2-ip/swagger-ui/`
- **H2 콘솔**: `http://your-ec2-ip/h2-console/`

## 🔒 **6단계: 보안 설정**

### 6.1 RDS 보안 그룹 업데이트
1. RDS → 데이터베이스 → crypto-alert-db
2. 보안 그룹 ID 확인
3. EC2 보안 그룹에서 RDS 보안 그룹으로 MySQL 트래픽 허용

### 6.2 SSL 인증서 설정 (선택사항)
```bash
# Let's Encrypt로 SSL 인증서 발급
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

## 📊 **7단계: 모니터링 설정**

### 7.1 CloudWatch 로그 설정
```bash
# CloudWatch 에이전트 설치
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb
```

## 🎯 **배포 완료!**

이제 `http://your-ec2-ip`로 접속하여 Crypto Alert System을 사용할 수 있습니다.

### 📝 **주요 포인트:**
- RDS와 EC2는 같은 VPC에 있어야 함
- 보안 그룹에서 필요한 포트만 열어둠
- 환경변수는 실제 값으로 설정
- SSL 인증서로 HTTPS 적용 권장
