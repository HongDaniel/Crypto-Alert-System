# 암호화폐 알림 시스템 (Crypto Alert System)

Spring Boot와 React로 구축된 포괄적인 암호화폐 공포&탐욕 지수 알림 시스템입니다.

## 🚀 주요 기능

### 백엔드 (Spring Boot)
- **멀티모듈 아키텍처**: 도메인, 서비스, 인프라, API 모듈로 관심사 분리
- **JWT 인증**: HttpOnly 쿠키를 사용한 안전한 사용자 인증
- **공포&탐욕 지수 통합**: 실시간 암호화폐 시장 심리 모니터링
- **이메일 알림**: Thymeleaf를 사용한 아름다운 HTML 이메일 템플릿
- **SMS 알림**: Solapi를 통한 SMS 알림 통합
- **알림 관리**: 알림 조건 생성, 업데이트 및 관리
- **알림 히스토리**: 상세한 로그와 함께 모든 알림 실행 추적
- **스케줄된 알림**: 매일 오전 9시 자동 알림 실행
- **수동 알림 실행**: 온디맨드 알림 트리거

### 프론트엔드 (React)
- **모던 UI**: 일관된 스타일링의 깔끔하고 반응형 디자인
- **대시보드**: 실시간 공포&탐욕 지수 표시 및 히스토리 차트
- **알림 설정**: 알림 조건 및 채널의 쉬운 구성
- **활성 알림 관리**: 활성 알림 보기, 편집 및 삭제
- **알림 히스토리**: 메시지 내용 팝업이 있는 상세 히스토리
- **계정 관리**: 사용자 프로필 관리 및 설정
- **반응형 디자인**: 데스크톱, 태블릿, 모바일 기기에 최적화

## 🏗️ 아키텍처

```
Crypto-Alert-multiModule/
├── api/                 # REST API 컨트롤러 및 설정
├── common/              # 공유 유틸리티 및 예외
├── domain/              # 엔티티 모델 및 리포지토리
├── infra/               # 인프라 설정
├── service/             # 비즈니스 로직 및 서비스
└── frontend/            # React 프론트엔드 애플리케이션
```

## 🛠️ 기술 스택

### 백엔드
- **Java 17**
- **Spring Boot 2.7.12**
- **Spring Security** with JWT
- **Spring Data JPA** with Hibernate
- **H2 Database** (인메모리)
- **Thymeleaf** 이메일 템플릿용
- **JavaMail** 이메일 발송용
- **Solapi** SMS 알림용
- **Gradle** 빌드 관리용

### 프론트엔드
- **React 18**
- **Axios** API 통신용
- **CSS3** 모던 스타일링
- **반응형 디자인**

## 🚀 시작하기

### 사전 요구사항
- Java 17 이상
- Node.js 16 이상
- npm 또는 yarn

### 백엔드 설정
1. 프로젝트 루트로 이동
2. 백엔드 실행:
```bash
./gradlew :api:bootRun
```

백엔드는 `http://localhost:8080`에서 시작됩니다.

### 프론트엔드 설정
1. 프론트엔드 디렉토리로 이동:
```bash
cd frontend
```

2. 의존성 설치:
```bash
npm install
```

3. 개발 서버 시작:
```bash
npm start
```

프론트엔드는 `http://localhost:3000`에서 시작됩니다.

## 📱 기능 개요

### 대시보드
- 실시간 공포&탐욕 지수 표시
- 히스토리 지수 차트
- 현재 시장 상태

### 알림 관리
- 사용자 정의 알림 조건 생성
- 임계값 설정 (0-100)
- 알림 유형 선택 (이상/이하)
- 알림 채널 선택 (이메일/SMS)
- 기존 알림 편집 및 삭제

### 알림 히스토리
- 실행된 모든 알림 보기
- 상세한 메시지 내용 확인
- 사용된 알림 채널 추적
- 날짜 및 유형별 필터링

### 계정 설정
- 사용자 프로필 정보 업데이트
- 비밀번호 변경
- 연락처 정보 관리
- 계정 상태 보기

## 🔧 설정

### 이메일 설정
`application.yml`에 이메일 설정을 업데이트하세요:
```yaml
mail:
  host: smtp.gmail.com
  port: 587
  username: your-email@gmail.com
  password: your-app-password
```

### SMS 설정
`application.yml`에 Solapi 설정을 업데이트하세요:
```yaml
solapi:
  api-key: YOUR_SOLAPI_API_KEY
  api-secret: YOUR_SOLAPI_API_SECRET
  from: YOUR_SENDER_PHONE_NUMBER
```

## 📊 API 엔드포인트

### 인증
- `POST /api/auth/login` - 사용자 로그인
- `POST /api/auth/logout` - 사용자 로그아웃
- `GET /api/auth/me` - 현재 사용자 정보 조회
- `PUT /api/auth/me` - 사용자 정보 업데이트

### 알림
- `GET /api/alerts` - 모든 알림 조회
- `POST /api/alerts` - 새 알림 생성
- `PUT /api/alerts/{id}` - 알림 업데이트
- `DELETE /api/alerts/{id}` - 알림 삭제
- `POST /api/alert/send` - 수동 알림 실행

### 알림 히스토리
- `GET /api/alert-history` - 알림 히스토리 조회
- `GET /api/alert-history/{id}` - 알림 히스토리 상세 조회
- `DELETE /api/alert-history/{id}` - 알림 히스토리 삭제

## 🎨 UI 컴포넌트

### 주요 컴포넌트
- **Dashboard**: 시장 개요가 있는 메인 랜딩 페이지
- **AlertSettingsCard**: 새 알림 조건 생성
- **ActiveAlertList**: 기존 알림 관리
- **AlertHistoryCard**: 알림 실행 히스토리 보기
- **AccountPage**: 사용자 프로필 관리

### 디자인 특징
- 일관된 색상 체계 및 타이포그래피
- 반응형 그리드 레이아웃
- 인터랙티브 버튼 및 폼
- 확인을 위한 모달 대화상자
- 로딩 상태 및 오류 처리

## 🔒 보안

- JWT 기반 인증
- 토큰 저장을 위한 HttpOnly 쿠키
- BCrypt를 사용한 비밀번호 암호화
- 프론트엔드 통합을 위한 CORS 설정
- 입력 검증 및 살균화

## 📈 향후 개선사항

- [ ] 푸시 알림
- [ ] 다중 암호화폐 지원
- [ ] 고급 차트 및 분석
- [ ] 사용자 역할 및 권한
- [ ] 이메일 템플릿 사용자 정의
- [ ] API 속도 제한
- [ ] 데이터베이스 마이그레이션 스크립트
- [ ] Docker 컨테이너화

## 🤝 기여하기

1. 저장소 포크
2. 기능 브랜치 생성
3. 변경사항 적용
4. 테스트 추가 (해당하는 경우)
5. 풀 리퀘스트 제출

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다. 자세한 내용은 LICENSE 파일을 참조하세요.

## 👨‍💻 작성자

**HongDaniel**
- GitHub: [@HongDaniel](https://github.com/HongDaniel)

## 🙏 감사의 말

- 공포&탐욕 지수 데이터 제공: [Alternative.me](https://alternative.me/crypto/fear-and-greed-index/)
- 이메일 템플릿은 모던 디자인 원칙에서 영감을 받았습니다
- 접근성을 고려하여 구축된 React 컴포넌트
