#!/bin/bash

# Crypto Alert Application EC2 배포 스크립트

echo "🚀 Crypto Alert 애플리케이션 배포를 시작합니다..."

# 환경 변수 설정
APP_NAME="crypto-alert"
JAR_FILE="api-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="crypto-alert"
DEPLOY_PATH="/home/ubuntu/crypto-alert-deploy"
BACKUP_PATH="/home/ubuntu/backups"
LOG_PATH="/var/log/crypto-alert"

# 백업 디렉토리 생성
mkdir -p $BACKUP_PATH
mkdir -p $LOG_PATH

# 기존 서비스 중지
echo "⏹️ 기존 서비스 중지 중..."
sudo systemctl stop $SERVICE_NAME 2>/dev/null || true

# 기존 JAR 파일 백업
if [ -f "$DEPLOY_PATH/$JAR_FILE" ]; then
    echo "💾 기존 JAR 파일 백업 중..."
    cp "$DEPLOY_PATH/$JAR_FILE" "$BACKUP_PATH/${JAR_FILE}.$(date +%Y%m%d_%H%M%S)"
fi

# 새로운 JAR 파일로 교체
echo "📦 새로운 JAR 파일 배포 중..."
if [ -f "$DEPLOY_PATH/$JAR_FILE" ]; then
    # JAR 파일이 이미 복사되어 있다고 가정
    echo "✅ JAR 파일이 준비되었습니다."
else
    echo "❌ JAR 파일을 찾을 수 없습니다: $DEPLOY_PATH/$JAR_FILE"
    exit 1
fi

# systemd 서비스 파일 생성
echo "⚙️ systemd 서비스 설정 중..."
sudo tee /etc/systemd/system/$SERVICE_NAME.service > /dev/null <<EOF
[Unit]
Description=Crypto Alert Application
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=$DEPLOY_PATH
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar $JAR_FILE
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=$SERVICE_NAME

# 환경 변수
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JAVA_OPTS=-Xms512m -Xmx1024m

[Install]
WantedBy=multi-user.target
EOF

# systemd 데몬 리로드
sudo systemctl daemon-reload

# 서비스 활성화 및 시작
echo "🔄 서비스 시작 중..."
sudo systemctl enable $SERVICE_NAME
sudo systemctl start $SERVICE_NAME

# 서비스 상태 확인
sleep 10
if sudo systemctl is-active --quiet $SERVICE_NAME; then
    echo "✅ 서비스가 성공적으로 시작되었습니다!"
    
    # 포트 확인
    if netstat -tlnp | grep :8080 > /dev/null; then
        echo "✅ 포트 8080에서 서비스가 실행 중입니다."
    else
        echo "⚠️ 포트 8080에서 서비스를 찾을 수 없습니다."
    fi
    
    # 헬스체크
    echo "🏥 헬스체크 실행 중..."
    sleep 30
    if curl -f http://localhost:8080/api/alert/test-sms > /dev/null 2>&1; then
        echo "✅ 헬스체크 성공! 애플리케이션이 정상적으로 동작합니다."
    else
        echo "❌ 헬스체크 실패! 애플리케이션 상태를 확인해주세요."
        echo "📋 서비스 로그:"
        sudo journalctl -u $SERVICE_NAME --no-pager -n 20
    fi
else
    echo "❌ 서비스 시작에 실패했습니다!"
    echo "📋 서비스 로그:"
    sudo journalctl -u $SERVICE_NAME --no-pager -n 20
    exit 1
fi

echo "🎉 배포가 완료되었습니다!"
echo "🌐 애플리케이션 URL: http://$(curl -s ifconfig.me):8080"
echo "📊 서비스 상태: sudo systemctl status $SERVICE_NAME"
echo "📋 서비스 로그: sudo journalctl -u $SERVICE_NAME -f"