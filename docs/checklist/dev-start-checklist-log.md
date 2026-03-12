# 개발 시작 체크리스트 — 처리 이력

체크리스트(`dev-start-checklist.md`) 각 항목의 상세 처리 내역입니다.

---

## 2026-03-04

### Java 17 설치 및 설정

- **상태**: 완료
- **내용**: Java 18 (Eclipse Temurin) 설치 및 JAVA_HOME 설정
- **비고**: 체크리스트에는 Java 17로 명시되어 있으나, Spring Boot 3.2.4는 Java 17 이상 지원이므로 Java 18 사용
- **변경 파일**: 시스템 환경 설정 (로컬 머신)

---

## 2026-03-05

### CLAUDE.md Spring Boot 버전 정보 수정

- **상태**: 완료
- **내용**: CLAUDE.md의 Spring Boot 버전을 `3.4 (Stable)` → `3.2.4`로 수정
- **사유**: build.gradle 실제 버전과 문서 간 불일치 해소
- **변경 파일**:
  - `CLAUDE.md` — 기술 스택 테이블의 Spring Boot 버전 수정

### .gitignore 보완

- **상태**: 완료
- **내용**: 기존 최소 구성에서 아래 패턴 추가
  - IDE: `.idea/`, `*.iml`, `*.iws` (IntelliJ)
  - OS: `.DS_Store`, `Thumbs.db`
  - 임시 파일: `*.log`, `*.tmp`, `*.swp`, `*.bak`
  - 프론트엔드: `node_modules/`, `client/dist/`, `client/.vite/`
  - 환경 파일: `.env`, `.env.local`, `*.key`, `*.pem`
  - 빌드 결과물: `*.jar`, `*.war` (`gradle-wrapper.jar` 제외)
- **변경 파일**:
  - `.gitignore`

### static/ 디렉토리 생성

- **상태**: 완료
- **내용**: `src/main/resources/static/` 디렉토리 생성
- **사유**: 개발 서버에서 Lit 클라이언트 정적 파일 서빙을 위한 경로 확보
- **변경 파일**:
  - `src/main/resources/static/` (신규 디렉토리)

### 환경 설정 파일 민감 정보 — Jasypt 암호화 적용

- **상태**: 완료
- **내용**: properties 파일 내 하드코딩된 DB/SMTP 비밀번호를 Jasypt로 암호화
- **적용 상세**:
  - 라이브러리: `jasypt-spring-boot-starter` 3.0.4 → 3.0.5 업그레이드 (Spring Boot 3.x 호환)
  - 알고리즘: `PBEWITHHMACSHA512ANDAES_256` (라이브러리 기본값)
  - IV 생성기: `RandomIvGenerator`
  - 마스터 키: 환경변수 `JASYPT_ENCRYPTOR_PASSWORD` (기본값: `operato-wcs-secret`)
  - 암호화 대상: DB 비밀번호, SMTP 비밀번호 (`ENC()` 래퍼 적용)
- **트러블슈팅**:
  1. 최초 `PBEWithMD5AndDES` 알고리즘으로 암호화 → DB 인증 실패 → 기본 알고리즘(`PBEWITHHMACSHA512ANDAES_256`)으로 재암호화
  2. 재암호화 후에도 실패 → jasypt-spring-boot-starter 3.0.4가 Spring Boot 3.x 미지원 → 3.0.5로 업그레이드하여 해결
- **변경 파일**:
  - `build.gradle` — jasypt-spring-boot-starter 3.0.5로 업그레이드
  - `src/main/resources/application.properties` — Jasypt 마스터 키 설정 추가
  - `src/main/resources/application-dev.properties` — DB/SMTP 비밀번호 ENC() 암호화
  - `src/main/resources/application-factory.properties` — 동일
  - `src/main/resources/application-demobox.properties` — 동일
  - `src/main/resources/application-ildong.properties` — 동일
  - `src/main/resources/application-operato2.properties` — 동일
  - `src/main/resources/application-postgres.properties` — 동일

### Git 브랜치 전략 및 커밋 규칙 정립

- **상태**: 완료
- **내용**: CLAUDE.md에 Git 브랜치 전략 및 커밋 메시지 규칙 추가
- **브랜치 전략**:
  - v1 완료 전: `main`에 직접 push
  - v1 완료 후: GitFlow 전환 (`develop`, `feature/*`, `fix/*`, `hotfix/*`), `main` 직접 push 금지, PR 통한 병합
- **커밋 메시지 규칙**:
  - 한국어 작성, 명사형 종결
  - 형식: `<요약> — <상세 설명>`
  - 예: `Jasypt 암호화 적용 — properties 파일 민감 정보 암호화 처리`
- **변경 파일**:
  - `CLAUDE.md` — Git 규칙 섹션

### docs 파일명 영어 kebab-case 통일

- **상태**: 완료
- **내용**: docs 폴더 내 파일명을 영어 소문자 kebab-case로 통일 (16개 파일 rename)
- **명명 규칙**:
  - 폴더명·파일명: 영어 소문자 kebab-case
  - 예외: `README.md`만 대문자 허용
  - 버전 구분: `-v2` 형식
- **주요 변경 예시**:
  - `SECURITY_IMPROVEMENTS.md` → `security-improvements.md`
  - `업무-프로세스.md` → `business-process.md`
  - `WCS-기능정의.md` → `wcs-feature-definition.md`
  - `smb-infrastructure-proposal2.md` → `smb-infrastructure-proposal-v2.md`
- **후속 작업**: CLAUDE.md docs 구조 업데이트, 문서 간 상호 참조 링크 수정 (10개 이상 파일)
- **변경 파일**:
  - 16개 docs 파일 (`git mv`)
  - `CLAUDE.md` — docs 디렉토리 구조 및 명명 규칙 추가
  - 상호 참조 링크가 포함된 문서 다수 수정

### Docker 문서 통합

- **상태**: 완료
- **내용**: `docs/operations/docker.md`와 `docs/operations/backend-docker.md` 내용 비교 후 `backend-docker.md`로 통합
- **사유**: docker.md 내용이 backend-docker.md의 부분집합이며, 향후 frontend-docker.md 추가를 고려하여 backend-docker.md로 통합
- **변경 파일**:
  - `docs/operations/backend-docker.md` — 통합된 Docker 배포 가이드
  - `docs/operations/docker.md` — 삭제
  - `CLAUDE.md` — docs 구조에서 docker.md 제거
  - `.claude/skills/docker-backend/SKILL.md` — 참조 경로 수정

### 빌드 성공 확인

- **상태**: 완료
- **내용**: `./gradlew clean build -x test` 실행, BUILD SUCCESSFUL (13초)
- **경고 사항** (동작 영향 없음):
  - `otarepo-core` — deprecated API 사용, unchecked 연산
  - `StageJobConfigUtil.java:459` — `@Deprecated` 어노테이션 누락
- **변경 파일**: 없음

---

## 2026-03-11

### 프론트엔드 구성 완료 (Things Factory 모노레포)

- **상태**: 완료
- **내용**: Things Factory 기반 Lerna 모노레포 구조로 프론트엔드 구성
- **구성 상세**:
  - 프레임워크: Things Factory (Lit 기반 Web Components)
  - 구조: Lerna 모노레포
  - 패키지:
    - `operato-wcs-ui` — 메인 WCS UI (포트 5908)
    - `operato-wcs-system-ui` — 시스템 UI
    - `metapage` — 메타페이지
    - `operatofill` — Spring 백엔드 연동
- **개발 서버**: `yarn wcs:dev` (포트 5908)
- **빌드**: `yarn build:client` → `dist-client/`
- **변경 파일**:
  - `frontend/` — 전체 디렉토리 구조 (Things Factory 소스)
  - `docs/architecture/frontend-structure.md` — 신규 생성, 프론트엔드 구조 및 빌드 가이드
  - `docs/checklist/dev-start-checklist.md` — 프론트엔드 섹션 완료 반영

### 백엔드 포트 통일 (8080 → 9190)

- **상태**: 완료
- **내용**: 운영 환경 백엔드 포트를 8080에서 9190으로 변경하여 개발/운영 환경 포트 통일
- **사유**: 개발 환경이 이미 9190 사용 중, 운영 환경도 동일 포트로 통일하여 설정 일관성 확보
- **변경 범위**:
  - Docker 설정 파일: `Dockerfile`, `Dockerfile.simple`, `docker-compose.yml`, `docker-compose.prod.yml`
  - 문서: `CLAUDE.md`, `docs/operations/deployment.md`, `docs/operations/backend-docker.md`, `docs/architecture/backend-architecture.md`, `docs/operations/smb-infrastructure-proposal.md`
  - 스킬: `.claude/skills/README.md`, `.claude/skills/run-backend/SKILL.md`, `.claude/skills/stop-backend/SKILL.md`, `.claude/skills/docker-backend/SKILL.md`
- **변경 파일**: 총 13개 파일의 23개 위치

### Nginx 운영 배포 환경 구성

- **상태**: 완료
- **내용**: 프론트엔드 운영 배포를 위한 Nginx + Docker 환경 구성
- **구성 상세**:
  - Nginx 설정: 정적 파일 서빙, API 프록시, 캐싱, 보안 헤더
  - Docker Compose: 백엔드(내부 9190) + Nginx(외부 80/443) 통합
  - 자동 배포 스크립트: 빌드 → Docker 배포 → 헬스 체크
- **변경 파일**:
  - `docker/nginx/nginx.conf` — 신규 생성, Nginx 웹 서버 설정
  - `docker-compose.prod.yml` — 신규 생성, 운영 환경 컨테이너 오케스트레이션
  - `scripts/deploy-prod.sh` — 신규 생성, 자동 배포 스크립트
  - `.dockerignore` — 프론트엔드 node_modules 제외 규칙 추가

### 배포 문서 통합 및 개선

- **상태**: 완료
- **내용**: `deployment.md`와 `frontend-deployment.md`를 단일 문서로 통합
- **개선 사항**:
  - 개발 환경: 백엔드(9190) / 프론트엔드(5908) 별도 실행으로 명확화
  - 운영 환경 옵션 1: JAR + Nginx 직접 실행 (상세 가이드)
  - 운영 환경 옵션 2: Docker 배포 (docker-compose.prod.yml 사용)
- **변경 파일**:
  - `docs/operations/deployment.md` — 완전히 재작성 (650+ 줄 종합 가이드)
  - `docs/operations/frontend-deployment.md` — 삭제 (deployment.md로 통합)
  - `docs/architecture/architecture.md` — 관련 문서에 프론트엔드 아키텍처 추가

### 체크리스트 현행화

- **상태**: 완료
- **내용**: `dev-start-checklist.md`를 최신 상태로 업데이트
- **업데이트 항목**:
  - 버전: 1.1 → 1.2
  - 최종 수정일: 2026-03-05 → 2026-03-11
  - 프론트엔드 구성 방향 결정: 미완료 → 완료로 체크
  - 프론트엔드 섹션: 플레이스홀더 → Things Factory 상세 구성 내용으로 대체
  - 현재 프로젝트 상태: Nginx 배포 환경, 포트 통일, 프론트엔드 구성 완료 추가
  - 변경 이력: 버전 1.2 항목 추가
- **변경 파일**:
  - `docs/checklist/dev-start-checklist.md`
  - `docs/checklist/dev-start-checklist-log.md` — 본 문서

### 코딩 컨벤션 백엔드/프론트엔드 분리

- **상태**: 완료
- **내용**: 단일 코딩 컨벤션 문서를 백엔드/프론트엔드로 분리
- **변경 사항**:
  - `coding-conventions.md` → `backend-coding-conventions.md`로 이름 변경
  - `frontend-coding-conventions.md` 신규 생성 (Things Factory/Lit 기반)
- **프론트엔드 컨벤션 주요 내용**:
  - Lit 컴포넌트 작성 규칙 (스타일 → 프로퍼티 → 상태 → 메서드 → render 순서)
  - 파일 네이밍: kebab-case (`gateway-page.js`)
  - TypeScript 타입 정의 및 사용 규칙
  - API 연동 패턴 (ApiClient)
  - 이벤트 처리 및 상태 관리
  - 성능 최적화 (shouldUpdate, repeat 디렉티브)
  - 접근성(A11y) 규칙
- **변경 파일**:
  - `docs/implementation/coding-conventions.md` → `backend-coding-conventions.md` (이름 변경)
  - `docs/implementation/frontend-coding-conventions.md` — 신규 생성
  - `docs/README.md` — 문서 구조 업데이트
  - `docs/checklist/dev-start-checklist.md` — 코딩 컨벤션 항목 완료로 체크, 미완료 항목 업데이트
  - `docs/checklist/dev-start-checklist-log.md` — 미완료 항목 설명 업데이트

---

## 미완료 항목

| 항목 | 우선순위 | 비고 |
|------|----------|------|
| 코딩 컨벤션 문서 보완 | 권장 | 백엔드/프론트엔드 분리 완료, 세부 내용 보완 필요 |
| API 명세 문서 작성 | 향후 | `docs/design/` 디렉토리 |
| 데이터베이스 ERD 및 스키마 문서 | 향후 | `docs/design/` 디렉토리 |
| UI/UX 설계 문서 | 향후 | `docs/design/` 디렉토리 |
