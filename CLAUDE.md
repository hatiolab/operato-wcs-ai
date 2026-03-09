# CLAUDE.md — Operato WCS AI

## 프로젝트 개요

**Operato WCS** — HatioLab이 개발한 물류 창고 제어 시스템. 입고→보관→출고→배송 프로세스의 설비·작업자를 제어·모니터링하는 현장 제어 시스템.

> 제품 목표, 라인업, 특장점 등 상세 내용은 [`docs/overview/overview.md`](docs/overview/overview.md) 참조.
> 시스템 아키텍처는 [`docs/architecture/architecture.md`](docs/architecture/architecture.md), 백엔드 아키텍처는 [`docs/architecture/backend-architecture.md`](docs/architecture/backend-architecture.md) 참조.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 백엔드 | Spring Boot 3.2.4 |
| 빌드 도구 | Gradle (최신 안정 버전) |
| 프론트엔드 | [Lit](https://lit.dev/) (Web Components) |
| 메시지 브로커 | RabbitMQ (WCS ↔ ECS 간 연동) |
| 개발 서버 | Spring Boot 내장 웹 서버 (정적 파일 서빙 포함) |
| 운영 배포 | Nginx 단독 또는 Nginx 포함 Docker 환경 |

---

## 프로젝트 구조 (예정)

```
operato-wcs-ai/
├── src/
│   ├── main/
│   │   ├── java/com/operato/wcs/      # Spring Boot 애플리케이션
│   │   └── resources/
│   │       ├── static/                # Lit 클라이언트 빌드 결과물 (개발 서빙용)
│   │       └── application.yml
│   └── test/
├── client/                            # Lit 클라이언트 소스 (사용자가 채워 넣음)
├── docker/
│   ├── nginx/
│   │   └── nginx.conf
│   └── Dockerfile
├── docs/                              # 프로젝트 산출물 문서 (상세 구조는 docs/README.md 참조)
├── docker-compose.yml
├── build.gradle
├── settings.gradle
└── CLAUDE.md
```

---

## 문서 구조

> 문서 디렉토리 구조, 파일 목록, 명명 규칙은 [`docs/README.md`](docs/README.md) 참조.
> `docs/` 하위에 폴더나 파일이 추가·변경·삭제되면 `docs/README.md`를 함께 업데이트할 것.

---

## 빌드 및 실행 명령어

- 빌드: `./gradlew build` | 테스트: `./gradlew test` | 실행: `./gradlew bootRun` | JAR: `./gradlew bootJar`
- 프론트엔드(`client/`): 구성 확정 후 추가 예정. 구조는 [`docs/architecture/frontend-structure.md`](docs/architecture/frontend-structure.md) 참조.
- 개발/운영 배포: [`docs/operations/deployment.md`](docs/operations/deployment.md) 참조.

---

## Git 규칙

- **절대로 사용자의 명시적 지시 없이 커밋하지 말 것**
- 커밋 전 반드시 빌드 및 테스트 통과 확인

### 브랜치 전략

**v1 완료 전 (현재):** `main`에 직접 push

### 커밋 메시지 규칙

```
<제목> — <상세 설명 (선택)>
```

**규칙:**
- 한국어로 작성
- 제목은 50자 이내, 명사형 종결 (예: "추가", "수정", "삭제")
- `—` (em dash) 뒤에 상세 설명 추가 가능
- Co-Authored-By 태그는 Claude Code 작업 시 자동 추가

---

## Claude Code Skills

> 스킬 목록, 사용법, 관리 규칙은 [.claude/skills/README.md](.claude/skills/README.md) 참조.
> 스킬 추가·수정·삭제 시 해당 README.md를 함께 업데이트할 것.

---

## 참고 사항

- 클라이언트 소스(`client/`)는 사용자가 직접 관리하며 Claude가 임의로 수정하지 않음
- 환경별 설정 분리: `application.yml` / `application-dev.yml` / `application-prod.yml`
