# Operato WCS AI — 개발 시작 전 체크리스트

## 문서 정보

| 항목 | 내용 |
|------|------|
| 작성일 | 2026-03-01 |
| 최종 수정일 | 2026-03-11 |
| 버전 | 1.2 |
| 목적 | 개발 시작 전 정리 및 준비 사항 체크리스트 |

---

## 개발 시작 전 체크리스트

### 즉시 처리 필요 (필수)
- [x] **Java 17 설치 및 설정** — Java 18 (Temurin) 설치 완료 (2026-03-04)
- [x] CLAUDE.md Spring Boot 버전 정보 수정 — 3.4 → 3.2.4로 수정 완료 (2026-03-05)
- [x] 빌드 성공 확인 (`./gradlew clean build`) — 완료 (2026-03-05)
- [x] .gitignore 보완 — IDE, OS, 프론트엔드, 환경 파일, 빌드 결과물 패턴 추가 완료 (2026-03-05)

### 권장 처리 (중요도 높음)
- [x] `src/main/resources/static/` 디렉토리 생성 — 완료 (2026-03-05)
- [x] 프론트엔드 구성 방향 결정 — Things Factory 기반 모노레포 구성 완료 (2026-03-11)
- [x] 환경 설정 파일 민감 정보 확인 — Jasypt 암호화 적용 완료 (2026-03-05)
- [x] 코딩 컨벤션 문서 작성 — 백엔드/프론트엔드 분리 완료 (2026-03-11)

### 향후 처리 가능
- [ ] API 문서 작성 (`docs/api/`)
- [ ] 데이터베이스 ERD 및 스키마 문서 (`docs/database/`)
- [ ] UI/UX 설계 문서 (`docs/design/`)
- [x] Git 브랜치 전략 및 커밋 규칙 정립 — CLAUDE.md에 추가 완료 (2026-03-05)

---

## 상세 내용

### 1. CLAUDE.md 업데이트 (완료)

- [x] Spring Boot 버전 `3.2.4`로 수정
- [x] docs/ 디렉토리 구조 및 파일 목록 추가
- [x] docs 명명 규칙 (영어 소문자 kebab-case) 추가
- [x] Git 브랜치 전략 및 커밋 메시지 규칙 추가
- [x] 프로젝트 구조 섹션을 실제 구조로 업데이트 — frontend/ 포함 (2026-03-11)
- [x] `frontend/` 디렉토리 구성 완료 — Things Factory 모노레포 (2026-03-11)

---

### 2. 프론트엔드 (frontend/) 준비 (완료)

**구성 완료 내용**:
- **프레임워크**: Things Factory (Lit 기반 Web Components)
- **구조**: Lerna 모노레포
- **패키지**:
  - `operato-wcs-ui` — 메인 WCS UI (포트 5908)
  - `operato-wcs-system-ui` — 시스템 UI
  - `metapage` — 메타페이지
  - `operatofill` — Spring 백엔드 연동

**개발 서버 실행**:
```bash
cd frontend
yarn install              # 의존성 설치 (처음 한 번)
yarn wcs:dev             # 개발 서버 실행 (포트 5908)
```

**빌드**:
```bash
cd frontend
yarn build:client        # 클라이언트 빌드 → dist-client/
```

**상세 문서**: [`docs/architecture/frontend-structure.md`](../architecture/frontend-structure.md)

---

### 3. 문서 보완 필요 항목

#### 이미 잘 정리된 문서
- `docs/architecture/backend-architecture.md` — 백엔드 아키텍처
- `docs/requirements/ecs-module-feature-definition.md` — ECS 모듈 기능 정의
- `docs/requirements/wcs-feature-definition.md` — WCS 기능 정의
- `docs/operations/backend-docker.md` — 백엔드 Docker 배포 가이드

#### 추가 작성 권장 문서

1. **API 문서** (`docs/api/`)
   - REST API 명세서
   - 엔드포인트 목록
   - 요청/응답 예시

2. **데이터베이스 문서** (`docs/database/`)
   - ERD (Entity Relationship Diagram)
   - 테이블 스키마 정의
   - 초기 데이터 스크립트

3. **코딩 컨벤션**
   - 백엔드: `docs/implementation/backend-coding-conventions.md` — Java 코딩 스타일, 패키지 네이밍 규칙
   - 프론트엔드: `docs/implementation/frontend-coding-conventions.md` — Lit/TypeScript 컴포넌트 작성 규칙

4. **UI/UX 설계** (`docs/design/`)
   - 화면 정의서 (wireframe)
   - 사용자 시나리오

---

### 4. 빌드 및 환경 설정 확인

#### .gitignore 보완 (완료)
IDE, OS, 프론트엔드, 환경 파일(.env, *.key), 빌드 결과물(*.jar) 패턴 추가 완료.

#### 정적 파일 디렉토리 (완료)
`src/main/resources/static/` 디렉토리 생성 완료.

#### 환경 설정 파일 민감 정보 (완료)
- Jasypt 암호화 적용 완료 (jasypt-spring-boot-starter 3.0.5)
- 알고리즘: `PBEWITHHMACSHA512ANDAES_256`
- 마스터 키: 환경변수 `JASYPT_ENCRYPTOR_PASSWORD`로 관리
- 적용 대상: 6개 properties 파일의 DB/SMTP 비밀번호

---

## 현재 프로젝트 상태 (2026-03-11 기준)

#### 완료된 항목
- Spring Boot 백엔드 소스 추가 완료
- Docker 설정 파일 구성 완료 (Dockerfile, docker-compose.yml, docker-compose.prod.yml)
- Nginx 운영 배포 환경 구성 완료 (docker/nginx/nginx.conf)
- 프론트엔드 Things Factory 모노레포 구성 완료 (frontend/)
- 주요 문서 작성 완료 (아키텍처, 요구사항, 품질 가이드, 배포 가이드)
- Claude Code 스킬 구성 완료 (deploy, docker-backend, run-backend, stop-backend, log)
- Java 18 환경 설정 완료
- Jasypt 암호화 적용 완료
- docs 파일명 영어 kebab-case 통일 완료
- Git 브랜치 전략 및 커밋 규칙 정립 완료
- 포트 통일 완료 (개발/운영 모두 백엔드 9190, 프론트엔드 5908)
- 코딩 컨벤션 백엔드/프론트엔드 분리 완료 (backend-coding-conventions.md, frontend-coding-conventions.md)

#### 미완료 항목
- API 문서 보완 (docs/implementation/api/ — 5개 모듈 API 목록 작성 완료, 상세 명세 보완 필요)
- DB 스키마 문서 (docs/design/database-specification.md — 스키마 정의 완료, ERD 다이어그램 추가 필요)

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2026-03-01 | 초기 작성 | Claude Code |
| 1.1 | 2026-03-05 | 완료 항목 반영, 파일 경로 현행화, 불필요 섹션 정리 | Claude Code |
| 1.2 | 2026-03-11 | 프론트엔드 구성 완료 반영 (Things Factory), 포트 통일, Nginx 배포 환경 추가 | Claude Code |
