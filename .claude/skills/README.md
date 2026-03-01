# Claude Code Skills — Operato WCS AI

이 디렉토리는 Operato WCS AI 프로젝트를 위한 **Claude Code 스킬(Skills)** 모음입니다.

## 스킬이란?

Claude Code 스킬은 반복적이고 구조화된 작업을 자동화하기 위한 **재사용 가능한 프롬프트 템플릿**입니다. 사용자가 `/스킬명` 형태로 호출하면 Claude가 미리 정의된 절차에 따라 작업을 수행합니다.

### 스킬의 장점

- **일관성**: 동일한 작업을 항상 같은 방식으로 수행
- **효율성**: 복잡한 절차를 한 번의 명령으로 실행
- **안전성**: 사전 정의된 규칙과 검증 로직으로 실수 방지
- **재사용성**: 프로젝트 전반에서 반복되는 작업 패턴을 표준화

---

## 사용 가능한 스킬

### 1. `/commit` — Git 커밋 자동화

Git에 변경 사항을 안전하게 커밋합니다.

**주요 기능:**
- 변경 내용 자동 분석 및 커밋 메시지 생성
- 민감한 파일(`.env`, `credentials.json` 등) 자동 제외
- CLAUDE.md의 Git 안전 규칙 준수
- `Co-Authored-By: Claude Sonnet 4.5` 태그 자동 추가

**사용법:**
```bash
# 변경 내용을 분석하여 자동 커밋
/commit

# 커밋 메시지를 직접 지정
/commit Add user authentication feature
```

**참고:**
- [commit/SKILL.md](commit/SKILL.md) — 상세 스킬 정의

---

### 2. `/deploy-backend` — 백엔드 빌드 및 배포

Spring Boot 애플리케이션을 빌드하여 배포 가능한 JAR 파일을 생성합니다.

**주요 기능:**
- Java 버전 자동 확인 및 전환 (Spring Boot 3.2.4는 Java 17+ 필요)
- Gradle 빌드 실행 (`./gradlew clean build`)
- 생성된 JAR 파일 검증 (파일 크기, 존재 여부)
- 빌드 결과 상세 보고

**사용법:**
```bash
# 기본 빌드 (테스트 스킵, clean 포함)
/deploy-backend

# 테스트 포함 빌드
/deploy-backend --with-tests

# 증분 빌드 (clean 스킵, 빠른 빌드)
/deploy-backend --no-clean
```

**빌드 결과물:**
- `build/libs/operato-wcs-ai.jar` — 실행 가능한 JAR (약 150-160MB)
- `build/libs/operato-wcs-ai-plain.jar` — 의존성 제외 JAR

**참고:**
- [deploy-backend/SKILL.md](deploy-backend/SKILL.md) — 상세 스킬 정의

---

### 3. `/run-backend` — 백엔드 서버 실행

Spring Boot 애플리케이션을 로컬 환경에서 실행합니다.

**주요 기능:**
- Gradle bootRun 또는 사전 빌드된 JAR 파일 실행
- Spring 프로파일 선택 (dev, prod 등)
- 포트 설정 및 충돌 확인
- 디버그 모드 지원 (포트 5005)
- 헬스 체크 자동 수행

**사용법:**
```bash
# 기본 실행 (Gradle bootRun)
/run-backend

# JAR 파일 직접 실행
/run-backend --jar

# 프로파일 지정
/run-backend --jar --profile=prod

# 포트 변경
/run-backend --port=9090

# 디버그 모드
/run-backend --debug
```

**접속 정보:**
- 기본 URL: http://localhost:8080
- 헬스 체크: http://localhost:8080/actuator/health
- 디버그 포트: 5005 (디버그 모드 시)

**참고:**
- [run-backend/SKILL.md](run-backend/SKILL.md) — 상세 스킬 정의

---

### 4. `/docker-backend` — Docker 이미지 빌드 및 실행

Spring Boot 백엔드를 Docker 이미지로 빌드하고 컨테이너로 실행합니다.

**주요 기능:**
- Docker Compose를 이용한 빌드 및 실행 (기본값)
- 멀티스테이지 빌드 또는 사전 빌드된 JAR 사용 옵션
- 컨테이너 상태 및 헬스 체크 자동 검증
- Nginx 설정 포함 (운영 환경)

**사용법:**
```bash
# 기본 실행 (Docker Compose)
/docker-backend

# 빠른 빌드 (사전 빌드된 JAR 사용)
/docker-backend --simple

# 빌드만 수행 (실행 안 함)
/docker-backend --build-only

# 전체 재빌드 (기존 컨테이너 및 이미지 삭제 후 빌드)
/docker-backend --clean
```

**일반적인 워크플로우:**
```bash
# 1. JAR 빌드
/deploy-backend

# 2. Docker 이미지 빌드 및 실행
/docker-backend --simple

# 3. 로그 확인
docker logs -f operato-wcs-backend
```

**참고:**
- [docker-backend/SKILL.md](docker-backend/SKILL.md) — 상세 스킬 정의
- `docker/Dockerfile` — 멀티스테이지 빌드 정의
- `docker/Dockerfile.simple` — 사전 빌드된 JAR 사용
- `docker-compose.yml` — Docker Compose 설정

---

### 5. `/log` — 작업 로그 기록

현재 세션에서 수행한 작업 내용을 로그 파일로 기록합니다.

**주요 기능:**
- 오늘 날짜 파일(`.ai/logs/YYYY-MM-DD.md`)에 작업 내용 자동 기록
- 수행 내용, 변경 파일, 비고를 구조화된 형식으로 기록
- 기존 파일이 있으면 하단에 append (덮어쓰기 안 함)

**사용법:**
```bash
# 현재 세션 작업 내용을 자동 요약하여 기록
/log

# 작업 요약 메시지를 직접 지정
/log Backend compile error fix and Docker setup
```

**로그 파일 위치:**
- `.ai/logs/2026-03-01.md` (예시)

**참고:**
- [log/SKILL.md](log/SKILL.md) — 상세 스킬 정의

---

## 일반적인 개발 워크플로우

### 시나리오 1: 로컬 개발 (빠른 실행 및 테스트)

```bash
# 1. 서버 실행 (Gradle bootRun)
/run-backend

# 2. 브라우저/Postman으로 API 테스트
# http://localhost:8080/api/...

# 3. 코드 수정 후 서버 재시작 (Ctrl+C → /run-backend)
```

### 시나리오 2: 백엔드 개발 및 커밋

```bash
# 1. 코드 수정 후 빌드
/deploy-backend

# 2. 변경 사항 커밋
/commit

# 3. 오늘 작업 내용 기록
/log
```

### 시나리오 3: JAR 빌드 후 운영 환경 테스트

```bash
# 1. 백엔드 빌드
/deploy-backend

# 2. JAR 실행 (운영 프로파일)
/run-backend --jar --profile=prod

# 3. 헬스 체크 및 테스트
curl http://localhost:8080/actuator/health
```

### 시나리오 4: Docker 환경 배포

```bash
# 1. 백엔드 빌드
/deploy-backend

# 2. Docker 이미지 빌드 및 실행
/docker-backend --simple

# 3. 변경 사항 커밋 (Dockerfile 등 수정한 경우)
/commit Update Docker configuration

# 4. 작업 로그 기록
/log Docker backend setup complete
```

### 시나리오 5: 전체 재빌드 (클린 빌드)

```bash
# 1. Docker 환경 전체 재빌드
/docker-backend --clean

# 2. 로그 기록
/log Complete rebuild with Docker
```

---

## 스킬 구조

각 스킬은 다음과 같은 표준 구조를 따릅니다:

```
.claude/skills/
├── commit/
│   └── SKILL.md          # YAML frontmatter + 마크다운 프롬프트
├── deploy-backend/
│   └── SKILL.md
├── run-backend/
│   └── SKILL.md
├── docker-backend/
│   └── SKILL.md
└── log/
    └── SKILL.md
```

**SKILL.md 형식:**
```yaml
---
name: skill-name
description: 스킬 설명
disable-model-invocation: false
argument-hint: "[옵션 힌트]"
---

# 스킬 제목

스킬 실행 절차 및 상세 설명...
```

---

## 스킬 작성 규칙

새로운 스킬을 추가할 때는 다음 규칙을 따릅니다:

1. **명명 규칙**
   - 형식: `<동사>-<대상>` 또는 `<기능>-<타겟>`
   - 예시: `deploy-backend`, `docker-backend`, `commit`, `log`
   - 소문자, 하이픈 구분 (kebab-case)

2. **파일 구조**
   - `.claude/skills/<skill-name>/SKILL.md` — 단일 파일로 구성
   - YAML frontmatter + 마크다운 프롬프트 형식
   - `skill.json`, `README.md` 등 추가 파일 불필요

3. **내용 구성**
   - 실행 절차를 명확하고 구조화된 단계로 작성
   - 에러 처리 및 주의사항 명시
   - 사용 예시 및 출력 형식 제공

4. **일반성 원칙**
   - 특정 케이스에만 작동하는 하드코딩된 솔루션 지양
   - 다양한 상황에 대응 가능한 일반적인 프로세스 지향
   - 프로젝트별 특수성은 CLAUDE.md에서 처리

---

## 참고 자료

- [CLAUDE.md](../CLAUDE.md) — 프로젝트 전체 컨벤션 및 규칙
- [Claude Code 공식 문서](https://github.com/anthropics/claude-code) — Claude Code 사용법
- [docs/](../docs/) — 프로젝트 아키텍처 및 요구사항 문서

---

## 버전 정보

- **프로젝트**: Operato WCS AI
- **마지막 업데이트**: 2026-03-01
- **작성자**: Claude Sonnet 4.5

---

## 라이선스

이 스킬들은 Operato WCS AI 프로젝트의 일부이며, 프로젝트와 동일한 라이선스를 따릅니다.
