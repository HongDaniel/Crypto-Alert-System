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
ENV DATABASE_URL="jdbc:mysql://alert-system.cgqysgqck9ka.ap-northeast-2.rds.amazonaws.com:3306/crypto_alert?useSSL=true&serverTimezone=UTC"
ENV DATABASE_USERNAME="admin"
ENV DATABASE_PASSWORD="your-rds-password"

# 포트 노출
EXPOSE 8080

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/alert/test-sms || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
