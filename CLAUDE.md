# CLAUDE.md — Operato WCS AI

## 프로젝트 개요

**Operato WCS (Warehouse Control System)** 는 HatioLab이 개발한 물류 창고 제어 시스템 제품입니다.
백엔드는 Spring Boot, 프론트엔드는 Lit(Web Components) 기반으로 구성됩니다.

### 시스템 목적

Operato WCS는 풀필먼트 센터(Fulfillment Center)에서 이루어지는 **입고 → 보관 → 출고 → 배송** 프로세스 전반에 걸쳐 사람과 설비의 작업을 소프트웨어적으로 **제어·모니터링**하여 최대의 운영 퍼포먼스를 지원하는 **현장 제어 시스템**입니다.

### 제품 목표

끊임없이 변화하는 물류 환경에 유연하고 신속하게 대응 가능한 **다목적 물류 시스템 구축**을 최종 목표로 합니다.

| 방향 | 핵심 내용 |
|------|----------|
| 설비 최적화 운영 | 자동화 설비의 모듈화(플러그인), 설비 Capa 기반 최적 작업 분배, 분류 설비 가동율 극대화 |
| 통합 모니터링 | 센터 전체 설비·재고 실시간 통합 모니터링 (3D 재고 모니터링 포함) |
| 유연하고 지속적인 관리 | 프로세스 변화에 신속 대응, 작업 이력 데이터 기반 설비 Capa 지속 업데이트 |

### Operato 제품 라인업

Operato WCS는 HatioLab의 물류 운영 플랫폼 중 **현장 제어(Execution)** 계층에 위치합니다.

| 제품 | 구분 | 설명 |
|------|------|------|
| Operato Visualizer | Visualization | 물류 센터 가시화 솔루션 |
| Operato WMS | Execution | 창고 관리 시스템 (WCS의 상위 시스템) |
| **Operato WCS** | **Execution** | **창고 제어 시스템 (본 프로젝트)** |
| Operato ECS | Execution | 설비 제어 시스템 (WCS와 연동되는 물리 설비 제어) |

### 제품 특장점

| 특장점 | 설명 |
|--------|------|
| Web 기반 프레임워크 | 운영자·작업자·현황판 UI 통합 제공, Responsive UI 및 PWA로 모바일 환경 지원 |
| 물류 특화 프레임워크 | 물류 작업 처리 컨테이너 기반, 자동화 설비를 플러그인 형태로 추가 |
| 설비 통합 운영 및 최적화 | 주문·작업 형태에 따라 최적 설비에 분배, 설비 가동율 극대화로 확실한 ROI 보장 |
| 높은 유연성 | 설비군별·작업 형태별 설정 프로파일 제공, 동적 커스텀 서비스 기반 프로세스 제어 |
| 통합 모니터링 제공 | 설비 상황 및 재고 현황을 웹 기반 모니터링 보드로 실시간 확인 |
| 물류환경 선진화 | 모바일, 로봇, AGV, IoT, 클라우드 기술을 활용한 스마트 물류환경 지원 |

### 시스템 전체 구조

```
                    ┌──────────────────────────────────┐
                    │    Legacy WMS / Operato WMS      │
                    │         (상위 시스템)             │
                    └──────────┬───────────────────────┘
                  마스터/주문 수신 ↓  ↑ 실적 전송

┌─────────────┐   ┌────────────────────────────────────────────────┐   ┌───────────────────┐
│  클라이언트  │   │                  OPERATO WCS                   │   │  Message          │
│   접속 단말  │   │                                                │   │  Middleware       │
│             │   │  ┌──────────────────────────────────────────┐  │   │  / Operato ECS   │
│  Manager    │   │  │        Warehouse Control System          │  │   │                   │
│  Kiosk      │   │  │  리소스(설비,작업자)관리 │ 최적 작업 분배  │  │   │  - Gateway        │
│  PDA        │◀─▶│  │  설비 통합관리          │ 통합 모니터링   │  │◀─▶│  - Indicator      │
│  Tablet     │   │  └──────────────────────────────────────────┘  │   │  - Sorter         │
│  Monitoring │   │                                                │   │  - Cart           │
│  Board      │   │  ┌──────────────────────────────────────────┐  │   │  - AGV            │
│             │   │  │           입/출고 설비 모듈               │  │   │  - AS/RS          │
│  (http/rest)│   │  │  입고PDA │Put-To-Light│Pick-To-Light│P-DAS│  │   │                   │
│             │   │  └──────────────────────────────────────────┘  │   │  (Message 기반)   │
│             │   │                                                │   └───────────────────┘
│             │   │  ┌──────────────────────────────────────────┐  │
│             │   │  │   분류 컨테이너 (분류 API, 플러거블 구조)  │  │
│             │   │  └──────────────────────────────────────────┘  │
│             │   │                                                │
│             │   │       HatioLab 애플리케이션 프레임워크          │
└─────────────┘   └────────────────────────────────────────────────┘
```

### WCS 핵심 기능

| 기능 | 설명 |
|------|------|
| 리소스(설비·작업자) 관리 | 현장 설비와 작업자를 등록·관리하고 작업 가용 상태를 추적 |
| 최적 작업 분배 | 주문 특성을 분석하여 최적의 설비 ECS에 작업을 자동 분배 |
| 설비 통합관리 | 연결된 모든 설비 ECS의 상태를 통합 관리 |
| 통합 모니터링 | 현장 설비와 작업자의 실시간 작업 현황을 통합 모니터링 |

### 주요 역할

| 역할 | 설명 |
|------|------|
| 주문 수신 | Legacy WMS 또는 Operato WMS로부터 입고·출고·보관·이동 주문 및 마스터 수신 |
| 주문 분배 | 주문 특성을 분석하여 최적의 설비 ECS에 작업 분배 |
| 실적 집계 | 각 설비 ECS로부터 처리 실적을 수집 및 집계 |
| 실적 보고 | 작업 마감 후 처리 실적을 WMS로 전송 |
| 제어·모니터링 | 현장 설비와 작업자의 작업 상태를 실시간으로 제어 및 모니터링 |

### 클라이언트 접속 단말

WCS UI에 접속하는 단말 유형은 다음과 같으며, 모두 **http/rest** 기반으로 통신합니다.

| 단말 | 용도 |
|------|------|
| Manager | 관리자 PC 화면 — 설정, 주문 관리, 모니터링 등 전반적인 운영 관리 |
| Kiosk | 현장 키오스크 — 작업자 로그인, 작업 지시 확인 등 |
| PDA | 현장 작업자 모바일 단말 — 입고, 피킹 등 현장 작업 처리 |
| Tablet | 현장 태블릿 — 작업 지시 및 확인용 |
| Monitoring Board | 현장 대형 모니터 — 실시간 작업 현황 표시 |

### 입/출고 설비 모듈

WCS 내부에서 입출고 작업을 처리하는 플러거블(Pluggable) 구조의 설비 모듈입니다.

| 모듈 | 설명 |
|------|------|
| 입고 PDA | PDA를 이용한 입고 작업 처리 |
| Put-To-Light | 랙의 셀을 주문에 매핑하고, 상품 스캔 시 해당 셀 표시기가 점등되면 셀에 상품을 분류하는 방식 (출고 / 반품 모두 지원) |
| Pick-To-Light | 랙의 셀에 상품을 배치하고, 박스(주문)가 컨베이어를 타고 구역에 도착·스캔되면 담을 상품의 셀 표시기가 점등되어 박스에 분류하는 방식 |
| P-DAS | 최소 인원의 작업자가 PDA를 이용해 소량의 출고 주문을 처리하는 시스템 |
| SMS | Sorter Management System — 고성능 자동 분류 설비(Sorter)를 제어하여 대량 분류 처리 |
| New Equip | 신규 설비 모듈 확장 가능 (플러거블 구조) |

> 각 설비 모듈은 하단의 **분류 컨테이너(분류 API 제공, 플러거블 구조)** 위에서 동작합니다.
> 상세 기능 정의는 [`docs/requirements/ECS-모듈-기능정의.md`](docs/requirements/ECS-모듈-기능정의.md) 참조.

### WCS ↔ ECS 모듈 인터페이스 (주문 분배)

WCS의 주문 분배는 **메시지가 아닌 DB(주문 테이블) 기반**으로 동작합니다.
WCS가 상위 시스템으로부터 받은 주문을 분석(또는 상위 시스템이 지정)하여 **주문 테이블에 ECS 종류 정보를 포함해 저장**하면, 각 ECS 모듈이 자신에게 해당하는 주문을 읽어 처리합니다.

```
┌─────────────────────────────────────────────────────────┐
│                    Operato WCS                          │
│                                                         │
│  주문 수신 → 분석/분류 → 주문 테이블 저장 (ECS 종류 포함) │
│                          │                              │
│              ┌───────────┴────────────┐                 │
│              ▼                        ▼                 │
│       Lighting ECS 모듈        Put-To-Light ECS 모듈    │
│       (해당 주문 읽기)          (해당 주문 읽기)          │
└─────────────────────────────────────────────────────────┘
```

### ECS 모듈 ↔ 물리 설비 인터페이스 (RabbitMQ)

ECS 모듈과 물리 설비 간의 실제 통신은 **RabbitMQ 메시지** 기반으로 이루어집니다.
예를 들어 Lighting ECS 모듈이 표시기(Indicator)를 제어하는 흐름은 다음과 같습니다.

```
┌───────────────────┐   ① 점등 요청 메시지   ┌──────────────┐   ② 메시지 전달   ┌─────────────┐
│  Lighting ECS     │ ─────────────────────▶ │   RabbitMQ   │ ────────────────▶ │  표시기     │
│  (ECS 모듈)       │                        │  (Message    │                   │ (Indicator) │
│                   │ ◀───────────────────── │  Middleware) │ ◀──────────────── │             │
└───────────────────┘   ④ 점등 완료 처리      └──────────────┘   ③ 점등 결과 메시지└─────────────┘
```

| 단계 | 방향 | 내용 |
|------|------|------|
| ① | ECS 모듈 → RabbitMQ | 물리 설비 제어 요청 메시지 발행 (ex: 표시기 점등 요청) |
| ② | RabbitMQ → 물리 설비 | 메시지 라우팅 및 전달 |
| ③ | 물리 설비 → RabbitMQ | 처리 결과 메시지 발행 (ex: 점등 완료) |
| ④ | RabbitMQ → ECS 모듈 | 결과 메시지 수신 후 시스템 내 상태 처리 |

### 연동 물리 설비 ECS 목록

| 물리 설비 ECS | 설명 |
|--------------|------|
| Gateway | 설비 연동 게이트웨이 |
| Indicator | 표시기(LED) 제어 설비 |
| Sorter | 자동 분류 설비 |
| Cart | 피킹 카트 설비 |
| AGV | 자율 주행 운반 로봇 |
| AS/RS | 자동 창고 시스템 (Automated Storage & Retrieval System) |

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 백엔드 | Spring Boot 3.4 (Stable) |
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
├── docs/                              # 프로젝트 산출물 문서
│   ├── architecture/                  # 시스템 아키텍처 설계 문서
│   ├── api/                           # API 명세 (REST API 등)
│   ├── requirements/                  # 요구사항 정의서
│   ├── design/                        # UI/UX 설계 및 화면 정의서
│   ├── database/                      # DB 스키마, ERD
│   └── operations/                    # 운영 가이드, 배포 절차
├── docker-compose.yml
├── build.gradle
├── settings.gradle
└── CLAUDE.md
```

---

## 프론트엔드 디렉토리 구조 (`client/`)

Lit + TypeScript + Vite 기준 권장 구조입니다.

```
client/
├── src/
│   ├── components/              # 재사용 가능한 공통 Lit 컴포넌트
│   │   ├── common/              # 버튼, 입력, 뱃지 등 기본 UI 요소
│   │   └── layout/              # 헤더, 사이드바, 푸터 등 레이아웃
│   │
│   ├── pages/                   # 페이지 단위 컴포넌트 (라우팅 단위)
│   │   ├── dashboard/           # 대시보드
│   │   ├── order/               # 주문 관리
│   │   ├── inventory/           # 재고 관리
│   │   ├── equipment/           # 설비 관리
│   │   └── report/              # 리포트
│   │
│   ├── services/                # API 호출 및 비즈니스 로직
│   │   ├── api/                 # fetch wrapper, endpoint 정의
│   │   └── auth/                # 인증/인가 로직
│   │
│   ├── stores/                  # 전역 상태 관리
│   │
│   ├── router/                  # 클라이언트 사이드 라우터
│   │
│   ├── styles/                  # 전역 스타일
│   │   ├── global.css
│   │   └── variables.css        # CSS Custom Properties (디자인 토큰)
│   │
│   ├── utils/                   # 공통 유틸리티 함수
│   ├── types/                   # TypeScript 타입/인터페이스 정의
│   └── index.ts                 # 앱 진입점
│
├── public/                      # 정적 에셋 (빌드 시 그대로 복사)
│   ├── favicon.ico
│   └── assets/
│       ├── images/
│       └── icons/
│
├── test/                        # 테스트
│   ├── unit/
│   └── e2e/
│
├── index.html                   # HTML 진입점
├── package.json
├── tsconfig.json
├── vite.config.ts               # Vite 빌드 설정
└── .env.example                 # 환경변수 예시
```

### 프론트엔드 명명 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 파일명 | kebab-case | `order-list-page.ts`, `wcs-button.ts` |
| 클래스명 | PascalCase | `OrderListPage`, `WcsButton` |
| Custom Element 태그 | kebab-case (하이픈 필수) | `<order-list-page>`, `<wcs-button>` |
| CSS Custom Property | `--wcs-` 접두사 | `--wcs-color-primary`, `--wcs-spacing-md` |
| 서비스/유틸 파일 | kebab-case | `order-api.ts`, `date-utils.ts` |

### 빌드 출력 경로

- 개발: `client/dist/` → Spring Boot가 `src/main/resources/static/`에서 서빙
- 운영: `client/dist/` → Nginx `html/` 또는 Docker 이미지에 복사

---

## 빌드 및 실행 명령어

### 백엔드 (Spring Boot)

```bash
# 의존성 확인 및 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 개발 서버 실행 (내장 Tomcat, 정적 파일 포함 서빙)
./gradlew bootRun

# 프로덕션 JAR 빌드
./gradlew bootJar
```

### 프론트엔드 (Lit 클라이언트)

> 클라이언트 소스는 사용자가 직접 `client/` 디렉토리에 채워 넣습니다.
> 빌드 명령어는 클라이언트 구성 확정 후 이곳에 추가합니다.

```bash
# (추후 추가 예정)
cd client
npm install
npm run build
```

---

## 개발 vs 운영 환경

### 개발 환경

- Spring Boot 내장 웹 서버가 API와 정적 파일(Lit 클라이언트)을 함께 서빙
- 정적 파일 경로: `src/main/resources/static/`
- 실행 방법: `./gradlew bootRun`

---

### 운영 환경 (옵션 1 — bootJar 직접 실행)

백엔드 JAR과 프론트엔드를 각각 빌드하여 서버에 직접 배포하는 방식입니다.

**배포 흐름**
```
1. 프론트엔드 빌드  →  client/dist/ 생성
2. 백엔드 빌드      →  build/libs/operato-wcs-ai.jar 생성
3. JAR + 프론트엔드 빌드 결과물을 서버에 업로드
4. Nginx가 정적 파일(Lit 클라이언트) 서빙 및 API 요청 프록시
5. Java로 JAR 직접 실행
```

**실행 명령어**
```bash
# 백엔드 빌드
./gradlew bootJar

# 운영 서버에서 실행
java -jar -Dspring.profiles.active=prod build/libs/operato-wcs-ai.jar
```

**Nginx 역할**
- Lit 클라이언트 정적 파일 서빙 (`/`)
- Spring Boot API 요청 프록시 (`/api/**` → `localhost:8080`)

---

### 운영 환경 (옵션 2 — Docker 배포)

백엔드와 Nginx를 Docker 컨테이너로 패키징하여 배포하는 방식입니다.

**배포 흐름**
```
1. 프론트엔드 빌드  →  client/dist/ 생성
2. Docker 이미지 빌드 (Spring Boot JAR + Nginx 포함)
3. docker-compose로 컨테이너 실행
```

**실행 명령어**
```bash
# Docker 이미지 빌드 및 실행
docker-compose up -d --build

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

**컨테이너 구성** (`docker-compose.yml`)
- `app` 컨테이너: Spring Boot JAR 실행 (포트 8080)
- `nginx` 컨테이너: 정적 파일 서빙 + API 프록시 (포트 80/443)

**관련 파일**
- `docker/Dockerfile` — Spring Boot 앱 이미지 정의
- `docker/nginx/nginx.conf` — Nginx 설정
- `docker-compose.yml` — 컨테이너 오케스트레이션

---

## Git 규칙

- **절대로 사용자의 명시적 지시 없이 커밋하지 말 것**
- 커밋 전 반드시 빌드 및 테스트 통과 확인
- 브랜치 전략, PR 규칙은 추후 확정 후 여기에 추가

---

## 코딩 컨벤션

> 추후 팀 합의 후 이곳에 추가 예정

---

## 참고 사항

- 클라이언트 소스(`client/`)는 사용자가 직접 관리하며 Claude가 임의로 수정하지 않음
- 환경별 설정 분리: `application.yml` / `application-dev.yml` / `application-prod.yml`
