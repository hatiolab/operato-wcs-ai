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

## 미완료 항목

| 항목 | 우선순위 | 비고 |
|------|----------|------|
| 프론트엔드 구성 방향 결정 | 권장 | Lit 프로젝트 초기화 시점 결정 필요 |
| 코딩 컨벤션 문서 작성 | 권장 | `docs/implementation/coding-conventions.md` 보완 |
| API 문서 작성 | 향후 | `docs/api/` 디렉토리 |
| 데이터베이스 ERD 및 스키마 문서 | 향후 | `docs/database/` 디렉토리 |
| UI/UX 설계 문서 | 향후 | `docs/design/` 디렉토리 |
