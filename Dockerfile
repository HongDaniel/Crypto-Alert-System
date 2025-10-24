# Multi-stage build for Crypto Alert Application
FROM eclipse-temurin:17-jdk-alpine as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build.gradle 파일들 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 각 모듈의 build.gradle 파일들 복사
COPY common/build.gradle common/
COPY domain/build.gradle domain/
COPY infra/build.gradle infra/
COPY service/build.gradle service/
COPY api/build.gradle api/

# 소스 코드 복사
COPY common/src common/src
COPY domain/src domain/src
COPY infra/src infra/src
COPY service/src service/src
COPY api/src api/src

# 권한 설정
RUN chmod +x gradlew

# 의존성 다운로드 및 빌드
RUN ./gradlew build -x test

# Production stage
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 필요한 패키지 설치
RUN apk add --no-cache curl

# JAR 파일 복사
COPY --from=builder /app/api/build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# .env 파일 복사 (있는 경우에만)
COPY .env* ./

# 환경변수 파일에서 읽어오기
RUN echo "환경변수 파일 로드 중..." && \
    if [ -f .env ]; then \
        echo "✅ .env 파일 발견"; \
        cat .env; \
    else \
        echo "⚠️ .env 파일이 없습니다"; \
    fi

# 포트 노출
EXPOSE 8080

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/alert/test-sms || exit 1

# 환경변수 로드 스크립트 생성
RUN echo '#!/bin/sh' > /app/load-env.sh && \
    echo 'if [ -f .env ]; then' >> /app/load-env.sh && \
    echo '  echo "환경변수 파일 로드 중..."' >> /app/load-env.sh && \
    echo '  set -a' >> /app/load-env.sh && \
    echo '  . ./.env' >> /app/load-env.sh && \
    echo '  set +a' >> /app/load-env.sh && \
    echo '  echo "환경변수 로드 완료"' >> /app/load-env.sh && \
    echo 'else' >> /app/load-env.sh && \
    echo '  echo "⚠️ .env 파일이 없습니다"' >> /app/load-env.sh && \
    echo 'fi' >> /app/load-env.sh && \
    echo 'exec java $JAVA_OPTS -jar app.jar' >> /app/load-env.sh && \
    chmod +x /app/load-env.sh

# 애플리케이션 실행
ENTRYPOINT ["/app/load-env.sh"]
