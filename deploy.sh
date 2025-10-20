#!/bin/bash

# Crypto Alert System 배포 스크립트
echo "🚀 Crypto Alert System 배포 시작..."

# 1. 백엔드 빌드
echo "📦 백엔드 빌드 중..."
./gradlew clean build -x test

# 2. 프론트엔드 빌드
echo "📦 프론트엔드 빌드 중..."
cd frontend
npm install
npm run build
cd ..

# 3. JAR 파일 생성
echo "📦 JAR 파일 생성 중..."
./gradlew bootJar

# 4. 배포 파일 준비
echo "📁 배포 파일 준비 중..."
mkdir -p deploy
cp api/build/libs/api.jar deploy/
cp -r frontend/build deploy/static

# 5. 환경변수 파일 복사 (있는 경우)
if [ -f ".env" ]; then
    cp .env deploy/
    echo "✅ 환경변수 파일 복사됨"
else
    echo "⚠️  .env 파일이 없습니다. env.example을 참고하여 생성하세요."
fi

echo "✅ 배포 준비 완료!"
echo "📁 deploy/ 폴더의 내용을 EC2 서버에 업로드하세요."
