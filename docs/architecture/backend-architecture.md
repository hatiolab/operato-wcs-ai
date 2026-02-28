# Operato WCS 백엔드 아키텍처

## 문서 정보

| 항목 | 내용 |
|------|------|
| 작성일 | 2026-03-01 |
| 버전 | 1.0 |
| 대상 시스템 | Operato WCS (Warehouse Control System) Backend |
| 기술 스택 | Spring Boot 3.2.4, Java 17, RabbitMQ, Redis, Elasticsearch |

---

## 1. 백엔드 시스템 개요

### 1.1 시스템 목적

Operato WCS 백엔드는 물류 창고 제어 시스템의 핵심 애플리케이션 서버로서 다음 기능을 제공합니다:

- **주문 관리**: WMS로부터 입고/출고/보관/이동 주문 수신 및 분배
- **설비 제어**: Gateway, Indicator 등 물리 설비와의 RabbitMQ 기반 통신
- **작업 분배**: 주문 특성 분석을 통한 최적 ECS 모듈 선택 및 작업 분배
- **실적 관리**: 작업 처리 실적 수집, 집계 및 WMS 전송
- **통합 모니터링**: 설비 상태, 재고 현황, 작업 진행 상태 실시간 제공
- **사용자 관리**: 관리자, 작업자 인증/인가 및 권한 관리

### 1.2 시스템 특징

| 특징 | 설명 |
|------|------|
| 모듈형 아키텍처 | 기능별 패키지 분리, ECS 모듈 플러거블 구조 |
| 이벤트 기반 통신 | Spring Event, RabbitMQ를 통한 비동기 메시징 |
| 다중 데이터 저장소 | RDBMS(주문/마스터), Redis(캐시/세션), Elasticsearch(로그/검색) |
| RESTful API | 모든 클라이언트(Manager, PDA, Kiosk)와 REST API 통신 |
| 실시간 처리 | 설비 상태 변화, 작업 진행 상황 실시간 반영 |
| 확장 가능 | 신규 ECS 모듈, 설비 타입 추가 용이 |

---

## 2. 아키텍처 레이어

Operato WCS는 **계층형 아키텍처(Layered Architecture)**와 **모듈형 아키텍처(Modular Architecture)**를 결합한 하이브리드 구조입니다.

### 2.1 레이어 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  (REST Controllers, Web Initializers, Exception Handlers)   │
│    - REST API 엔드포인트 제공                                 │
│    - HTTP 요청/응답 처리                                      │
│    - 클라이언트 인증/인가 검증                                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                           │
│  (Business Services, Event Handlers, Message Processors)    │
│    - 비즈니스 로직 구현                                        │
│    - 트랜잭션 관리                                            │
│    - 이벤트 발행/구독                                          │
│    - 외부 시스템 연동                                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  Persistence Layer                          │
│  (Entities, Repositories, ORM Manager)                      │
│    - 데이터 영속성 관리                                        │
│    - CRUD 연산 수행                                           │
│    - 쿼리 실행                                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   Data Source Layer                         │
│  (RDBMS, Redis, Elasticsearch, RabbitMQ)                    │
│    - MySQL/PostgreSQL/Oracle (주문, 마스터, 설정)             │
│    - Redis (캐시, 세션)                                       │
│    - Elasticsearch (로그, 검색)                               │
│    - RabbitMQ (메시지 큐)                                     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 횡단 관심사 (Cross-Cutting Concerns)

```
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│  (Config, Security, Cache, Logging, Exception Handling)      │
│    - Spring Security (인증/인가)                              │
│    - Aspect (로깅, 트랜잭션)                                  │
│    - Cache (Redis 기반)                                       │
│    - Exception Handling (전역 예외 처리)                      │
│    - Configuration (환경별 설정)                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 패키지 구조 및 모듈 설명

### 3.1 최상위 패키지 구조

```
src/main/java/
├── xyz.anythings.*        # WCS 비즈니스 도메인 모듈
├── xyz.elidom.*           # 공통 프레임워크 모듈
├── operato.*              # Operato 제품군 공통 모듈
├── net.sf.*               # 외부 유틸리티
├── org.*                  # 확장/수정된 외부 라이브러리
└── report.*               # 리포트 생성 관련 모듈
```

### 3.2 xyz.anythings 패키지 (WCS 비즈니스 도메인)

WCS의 핵심 비즈니스 로직을 담당하는 모듈입니다.

| 패키지 | 설명 | 주요 기능 |
|--------|------|----------|
| **xyz.anythings.boot** | 애플리케이션 부트스트랩 | - Spring Boot 애플리케이션 시작점<br>- 컴포넌트 스캔 설정<br>- 세션 쿠키 설정 |
| **xyz.anythings.base** | WCS 기본 도메인 | - 주문, 재고, 작업 엔티티<br>- 공통 비즈니스 서비스<br>- 유틸리티 |
| **xyz.anythings.gw** | Gateway & Indicator 관리 | - Gateway/Indicator 엔티티<br>- 표시기 제어 서비스<br>- 설비 배포 관리<br>- MQ 메시지 송수신 |
| **xyz.anythings.comm.rabbitmq** | RabbitMQ 통신 | - 메시지 큐 관리<br>- 이벤트 발행/구독<br>- 큐 네이밍 모델 |
| **xyz.anythings.sec** | 보안 (WCS 전용) | - 애플리케이션 엔티티<br>- WCS 보안 설정 |
| **xyz.anythings.sys** | 시스템 관리 (WCS 전용) | - 시스템 설정<br>- 캐시 관리<br>- 이벤트 처리<br>- 공통 유틸리티 |

### 3.3 xyz.elidom 패키지 (공통 프레임워크)

HatioLab 제품군 공통 프레임워크 모듈입니다.

| 패키지 | 설명 | 주요 기능 |
|--------|------|----------|
| **xyz.elidom.core** | 코어 프레임워크 | - 기본 엔티티 (BaseEntity)<br>- REST 기본 컨트롤러<br>- 공통 설정<br>- 유틸리티 |
| **xyz.elidom.base** | 기본 기능 | - 도메인 기본 모델<br>- 공통 시스템 서비스 |
| **xyz.elidom.orm** | ORM 관리 | - 엔티티 매니저<br>- 쿼리 빌더<br>- 트랜잭션 관리 |
| **xyz.elidom.dbist** | 데이터베이스 유틸리티 | - DDL/DML 처리<br>- 메타데이터 관리<br>- 어노테이션 기반 쿼리 |
| **xyz.elidom.rabbitmq** | RabbitMQ 클라이언트 | - RabbitMQ 연결 관리<br>- 메시지 송수신 클라이언트<br>- 메시지 추적(Trace)<br>- Dead Letter Queue 처리 |
| **xyz.elidom.sec** | 보안 (공통) | - Spring Security 설정<br>- 인증/인가<br>- JWT 처리<br>- 사용자/권한 관리 |
| **xyz.elidom.sys** | 시스템 (공통) | - 시스템 설정<br>- 캐시 관리<br>- 메시지 관리<br>- 배치 작업 |
| **xyz.elidom.job** | 배치 작업 | - Quartz 스케줄러<br>- 배치 작업 관리 |
| **xyz.elidom.msg** | 메시지 관리 | - 다국어 메시지<br>- 알림 메시지 |
| **xyz.elidom.mw** | 미들웨어 | - 프린터 연동<br>- 외부 시스템 연동 |
| **xyz.elidom.print** | 인쇄 관리 | - 라벨/바코드 인쇄<br>- 인쇄 템플릿 관리 |
| **xyz.elidom.dev** | 개발 도구 | - 개발 유틸리티<br>- 테스트 지원 |
| **xyz.elidom.exception** | 예외 처리 | - 클라이언트 예외<br>- 서버 예외<br>- 전역 예외 핸들러 |
| **xyz.elidom.util** | 유틸리티 | - 공통 유틸리티<br>- 변환기(Converter) |

### 3.4 operato 패키지

```
operato.core
├── config          # Operato 공통 설정
└── web            # 웹 초기화
    └── initializer # 애플리케이션 초기화 로직
```

### 3.5 모듈별 레이어 구조

각 모듈은 일관된 레이어 구조를 따릅니다:

```
xyz.anythings.{module}/
├── config/         # 설정 클래스 (Properties, Bean 정의)
├── entity/         # JPA 엔티티 (도메인 모델)
├── model/          # DTO, VO (데이터 전송 객체)
├── rest/           # REST 컨트롤러
├── service/        # 비즈니스 서비스
│   ├── api/        # 서비스 인터페이스
│   └── mq/         # 메시지 큐 처리
├── event/          # 이벤트 정의 및 핸들러
├── util/           # 유틸리티 클래스
└── web/            # 웹 초기화, 인터셉터
```

---

## 4. 핵심 컴포넌트

### 4.1 애플리케이션 시작점

**클래스**: `xyz.anythings.boot.AnythingsBootApplication`

```java
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = { "xyz.anythings.*", "xyz.elidom.*", "operato.*" })
@ImportResource({
    "classpath:/WEB-INF/application-context.xml",
    "classpath:/WEB-INF/dataSource-context.xml"
})
public class AnythingsBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnythingsBootApplication.class, args);
    }
}
```

**주요 설정**:
- 비동기 처리 활성화 (`@EnableAsync`)
- 스케줄링 활성화 (`@EnableScheduling`)
- 다중 패키지 컴포넌트 스캔
- XML 설정 파일 로드 (레거시 호환)

### 4.2 Gateway & Indicator 관리 모듈

**패키지**: `xyz.anythings.gw`

#### 4.2.1 주요 엔티티

| 엔티티 | 설명 |
|--------|------|
| `Gateway` | 물리 게이트웨이 정보 (IP, 포트, 상태) |
| `Indicator` | 표시기 정보 (Gateway에 속함) |
| `IndConfig` | 표시기 설정 프로파일 |
| `IndConfigSet` | 표시기 설정 세트 |
| `Deployment` | 설비 배포 정보 |

#### 4.2.2 주요 서비스

| 서비스 | 설명 |
|--------|------|
| `IndicatorDispatcher` | 표시기 명령 분배 (점등/소등) |
| `IndConfigProfileService` | 표시기 설정 프로파일 관리 |
| `IIndHandlerService` | 표시기 제어 인터페이스 |

#### 4.2.3 MQ 메시지 모델

표시기 제어를 위한 RabbitMQ 메시지 모델:

**점등 요청 흐름**:
```
WCS → RabbitMQ → Gateway/Indicator
LedOnRequest → LedOnRequestAck → LedOnResponse → LedOnResponseAck
```

**주요 메시지 타입**:
- `GatewayInitRequest/Response` - Gateway 초기화
- `IndicatorInitReport` - Indicator 초기화 보고
- `LedOnRequest/Response` - LED 점등 요청/응답
- `LedOffRequest/Response` - LED 소등 요청/응답
- `IndicatorOnRequest/Response` - 표시기 점등 (작업 정보 포함)
- `IndicatorOffRequest/Response` - 표시기 소등
- `IndicatorStatusReport` - 표시기 상태 보고
- `ErrorReport` - 에러 보고

### 4.3 RabbitMQ 통신 모듈

**패키지**: `xyz.elidom.rabbitmq`, `xyz.anythings.comm.rabbitmq`

#### 4.3.1 RabbitMQ 클라이언트

| 클래스 | 설명 |
|--------|------|
| `SystemClient` | RabbitMQ 시스템 클라이언트 |
| `CreateMessageReceiver` | 메시지 리시버 생성 |
| `MqReceiver` | 메시지 수신 처리 |
| `MqCommon` | 메시지 송수신 공통 로직 |

#### 4.3.2 메시지 추적

| 클래스 | 설명 |
|--------|------|
| `TracePublish` | 발행 메시지 추적 |
| `TraceDeliver` | 전달 메시지 추적 |
| `TraceDead` | Dead Letter 메시지 추적 |
| `TraceMessageConverter` | 메시지 변환 및 로깅 |

#### 4.3.3 큐 관리

| 클래스 | 설명 |
|--------|------|
| `BrokerSiteAdmin` | RabbitMQ 브로커 관리 |
| `MwQueueManageEvent` | 큐 관리 이벤트 |
| `LogisQueueNameModel` | 물류 큐 네이밍 모델 |

### 4.4 보안 모듈

**패키지**: `xyz.elidom.sec`

#### 4.4.1 인증/인가

| 컴포넌트 | 설명 |
|----------|------|
| Spring Security | 기본 인증/인가 프레임워크 |
| JWT | 토큰 기반 인증 (io.jsonwebtoken) |
| Session | Redis 기반 세션 관리 |

#### 4.4.2 주요 엔티티

| 엔티티 | 설명 |
|--------|------|
| `User` | 사용자 정보 |
| `Role` | 역할 정보 |
| `Permission` | 권한 정보 |

### 4.5 ORM 및 데이터 관리

**패키지**: `xyz.elidom.orm`, `xyz.elidom.dbist`

#### 4.5.1 ORM Manager

- 엔티티 생명주기 관리
- 쿼리 빌더 제공
- 트랜잭션 관리

#### 4.5.2 데이터베이스 유틸리티

- DDL 자동 생성 및 실행
- DML 헬퍼
- 메타데이터 추출
- 어노테이션 기반 쿼리

### 4.6 캐시 관리

**패키지**: `xyz.elidom.sys.cache`, `xyz.anythings.sys.cache`

#### 4.6.1 캐시 전략

| 캐시 유형 | 저장소 | 용도 |
|-----------|--------|------|
| Local Cache | ConcurrentHashMap | 애플리케이션 설정 |
| Distributed Cache | Redis | 세션, 공통 코드, 설비 상태 |

### 4.7 배치 작업

**패키지**: `xyz.elidom.job`

- Quartz 스케줄러 기반
- 배치 작업 등록/실행/모니터링
- 주기적 데이터 동기화, 통계 집계 등

---

## 5. 데이터 흐름

### 5.1 주문 처리 흐름

```
┌──────────┐
│ WMS/ERP  │ (상위 시스템)
└────┬─────┘
     │ ① 주문 수신 (REST API / File / MQ)
     ↓
┌──────────────────────────────────────┐
│  WCS Backend (Operato WCS)           │
│                                      │
│  ② 주문 분석 및 ECS 선택              │
│     (주문 타입, 설비 Capa 기반)       │
│                                      │
│  ③ 주문 테이블에 저장                 │
│     (ECS 정보 포함)                   │
└─────┬────────────────────────────────┘
      │
      ├─→ ④-A ECS 모듈 (Lighting)이 DB에서 자신의 주문 조회
      │
      ├─→ ④-B ECS 모듈 (Put-To-Light)이 DB에서 자신의 주문 조회
      │
      └─→ ④-C ECS 모듈 (P-DAS)가 DB에서 자신의 주문 조회
           │
           ↓ ⑤ 물리 설비에 작업 지시 (RabbitMQ)
┌──────────────────────────────────────┐
│  물리 설비 ECS                        │
│  (Indicator, Sorter, AGV, etc.)      │
└─────┬────────────────────────────────┘
      │ ⑥ 작업 완료 보고 (RabbitMQ)
      ↓
┌──────────────────────────────────────┐
│  WCS Backend                         │
│  ⑦ 작업 실적 업데이트                 │
│  ⑧ 실적 집계                          │
│  ⑨ WMS로 실적 전송                    │
└──────────────────────────────────────┘
```

### 5.2 설비 제어 흐름 (Indicator 점등 예시)

```
┌───────────────┐
│ 작업자 PDA    │
└───────┬───────┘
        │ ① 작업 시작 요청 (REST API)
        ↓
┌────────────────────────────────────────────┐
│  WCS Backend                               │
│                                            │
│  IndicatorDispatcher.lightOn()            │
│  ② Indicator 점등 메시지 생성              │
└────────┬───────────────────────────────────┘
         │ ③ RabbitMQ Publish
         ↓
┌────────────────────────────────────────────┐
│  RabbitMQ                                  │
│  Queue: wcs.indicator.on.request           │
└────────┬───────────────────────────────────┘
         │ ④ Message Routing
         ↓
┌────────────────────────────────────────────┐
│  Gateway (물리 설비)                        │
│  ⑤ Indicator 점등                          │
└────────┬───────────────────────────────────┘
         │ ⑥ 점등 완료 응답 (RabbitMQ Publish)
         ↓
┌────────────────────────────────────────────┐
│  RabbitMQ                                  │
│  Queue: wcs.indicator.on.response          │
└────────┬───────────────────────────────────┘
         │ ⑦ Message Consume
         ↓
┌────────────────────────────────────────────┐
│  WCS Backend                               │
│  MqReceiver.handleIndicatorOnResponse()   │
│  ⑧ 점등 상태 DB 업데이트                   │
│  ⑨ 이벤트 발행 (IndicatorOnEvent)         │
└────────────────────────────────────────────┘
```

### 5.3 클라이언트 요청 처리 흐름

```
┌──────────────┐
│ 클라이언트    │ (Manager, PDA, Kiosk, Monitoring Board)
└──────┬───────┘
       │ ① HTTP Request (REST API)
       ↓
┌──────────────────────────────────────────────────┐
│  Presentation Layer                              │
│  - Spring Security Filter (인증/인가 검증)        │
│  - REST Controller                               │
│    ② 요청 파라미터 검증 및 변환                   │
└──────┬───────────────────────────────────────────┘
       │ ③ 서비스 호출
       ↓
┌──────────────────────────────────────────────────┐
│  Service Layer                                   │
│  ④ 비즈니스 로직 실행                             │
│  ⑤ 트랜잭션 관리                                  │
│  ⑥ 이벤트 발행 (필요 시)                          │
└──────┬───────────────────────────────────────────┘
       │ ⑦ 데이터 조회/수정
       ↓
┌──────────────────────────────────────────────────┐
│  Persistence Layer                               │
│  ⑧ Entity 조회/저장/수정                          │
│  ⑨ 쿼리 실행                                      │
└──────┬───────────────────────────────────────────┘
       │ ⑩ SQL 실행
       ↓
┌──────────────────────────────────────────────────┐
│  DataSource (RDBMS, Redis, Elasticsearch)        │
└──────────────────────────────────────────────────┘
```

---

## 6. 메시지 기반 통신 (RabbitMQ)

### 6.1 RabbitMQ 역할

WCS와 물리 설비 ECS 간의 **비동기 메시지 통신** 담당:

- **설비 제어 명령** 전달 (WCS → ECS)
- **설비 상태 보고** 수신 (ECS → WCS)
- **에러 리포트** 수신 및 처리
- **메시지 추적** (Trace) 및 로깅
- **Dead Letter Queue** 처리

### 6.2 큐 네이밍 규칙

```
{system}.{module}.{action}.{type}

예시:
- wcs.gateway.init.request
- wcs.gateway.init.response
- wcs.indicator.on.request
- wcs.indicator.on.response
- wcs.indicator.off.request
- wcs.indicator.status.report
```

### 6.3 메시지 패턴

#### 6.3.1 Request-Response 패턴

```
WCS                      RabbitMQ                    ECS
 │                          │                         │
 ├─ Publish Request ───────→│                         │
 │                          ├─ Route ────────────────→│
 │                          │                         │
 │                          │←─── Publish Response ───┤
 │←────── Consume Response ─┤                         │
 │                          │                         │
```

#### 6.3.2 Report 패턴 (단방향)

```
ECS                      RabbitMQ                    WCS
 │                          │                         │
 ├─ Publish Report ────────→│                         │
 │                          ├─ Route ────────────────→│
 │                          │                         │
```

### 6.4 메시지 추적

모든 RabbitMQ 메시지는 추적(Trace) 테이블에 기록됩니다:

| 테이블 | 설명 |
|--------|------|
| `TracePublish` | WCS가 발행한 메시지 |
| `TraceDeliver` | WCS가 수신한 메시지 |
| `TraceDead` | Dead Letter Queue로 이동한 메시지 |

---

## 7. 기술 스택 상세

### 7.1 프레임워크 및 라이브러리

#### 7.1.1 Spring Boot 스타터

| 스타터 | 용도 |
|--------|------|
| `spring-boot-starter-web` | REST API, MVC |
| `spring-boot-starter-security` | 인증/인가 |
| `spring-boot-starter-data-rest` | REST 리포지토리 |
| `spring-boot-starter-data-redis` | Redis 연동 |
| `spring-boot-starter-data-elasticsearch` | Elasticsearch 연동 |
| `spring-boot-starter-amqp` | RabbitMQ 연동 |
| `spring-boot-starter-jdbc` | JDBC |
| `spring-boot-starter-cache` | 캐시 추상화 |
| `spring-boot-starter-quartz` | 배치 스케줄링 |
| `spring-boot-starter-mail` | 이메일 발송 |
| `spring-boot-starter-graphql` | GraphQL API |
| `spring-boot-starter-groovy-templates` | Groovy 템플릿 |

#### 7.1.2 데이터베이스 드라이버

| 드라이버 | 데이터베이스 |
|----------|--------------|
| `mysql-connector-j` | MySQL |
| `postgresql` (42.7.1) | PostgreSQL |
| `ojdbc11` | Oracle |

#### 7.1.3 보안

| 라이브러리 | 용도 |
|-----------|------|
| `spring-boot-starter-security` | Spring Security |
| `io.jsonwebtoken:jjwt-*` (0.12.3) | JWT 토큰 |
| `jasypt-spring-boot-starter` (3.0.4) | 암호화 |

#### 7.1.4 세션 관리

| 라이브러리 | 용도 |
|-----------|------|
| `spring-session-data-redis` | Redis 기반 세션 |
| `spring-session-jdbc` | JDBC 기반 세션 |

#### 7.1.5 HTTP 클라이언트

| 라이브러리 | 용도 |
|-----------|------|
| `httpclient5` (5.3) | HTTP 통신 |

#### 7.1.6 JSON 처리

| 라이브러리 | 용도 |
|-----------|------|
| `jackson-core` (2.16.1) | JSON 직렬화/역직렬화 |
| `gson` (2.10.1) | Google JSON 라이브러리 |
| `json-simple` (1.1.1) | 간단한 JSON 처리 |
| `fastjson` (1.2.47) | Alibaba JSON 라이브러리 |

#### 7.1.7 스크립팅

| 라이브러리 | 용도 |
|-----------|------|
| `groovy-jsr223` (5.0.0-alpha-4) | Groovy 스크립트 실행 |
| `jruby-engine` (1.1.7) | JRuby 스크립트 실행 |

#### 7.1.8 리포트 및 문서

| 라이브러리 | 용도 |
|-----------|------|
| `jxls` (2.14.0) | Excel 생성 |
| `poi` (5.2.5), `poi-ooxml` | Excel/Word 처리 |
| `pdfbox` (2.0.30) | PDF 생성/처리 |
| `jasperreports` (6.21.0) | 리포트 생성 |
| `openpdf` (1.3.35) | PDF 생성 |

#### 7.1.9 바코드/QR코드

| 라이브러리 | 용도 |
|-----------|------|
| `barcode4j` (2.1) | 바코드 생성 |
| `barbecue` (1.5-beta1) | 바코드 생성 |
| `zxing:core`, `zxing:javase` (3.4.1) | QR코드/바코드 생성 |

#### 7.1.10 SVG 처리

| 라이브러리 | 용도 |
|-----------|------|
| `batik-dom`, `batik-svggen`, `batik-bridge` (1.14) | SVG 생성/처리 |

#### 7.1.11 유틸리티

| 라이브러리 | 용도 |
|-----------|------|
| `hutool-all` (5.7.20) | 중국 유틸리티 라이브러리 |
| `commons-collections` (3.2.2) | 컬렉션 유틸리티 |
| `commons-dbcp` (1.4) | 커넥션 풀 |
| `javassist` (3.30.2-GA) | 바이트코드 조작 |
| `aspectjweaver` (1.9.21) | AOP |
| `velocity` (1.7) | 템플릿 엔진 |

### 7.2 데이터 저장소

| 저장소 | 용도 | 데이터 타입 |
|--------|------|-------------|
| **MySQL** | 주 데이터베이스 | 주문, 마스터, 설정, 작업, 실적 |
| **PostgreSQL** | 주 데이터베이스 (대안) | 주문, 마스터, 설정, 작업, 실적 |
| **Oracle** | 주 데이터베이스 (대안) | 주문, 마스터, 설정, 작업, 실적 |
| **Redis** | 캐시, 세션 | 공통 코드, 설비 상태, 사용자 세션 |
| **Elasticsearch** | 검색, 로그 | 메시지 로그, 작업 이력, 검색 인덱스 |

### 7.3 메시지 브로커

| 브로커 | 용도 | 통신 대상 |
|--------|------|-----------|
| **RabbitMQ** | 비동기 메시지 통신 | WCS ↔ Gateway/Indicator/Sorter/AGV 등 물리 설비 ECS |

---

## 8. 확장성 및 설계 원칙

### 8.1 확장 포인트

#### 8.1.1 ECS 모듈 플러거블 구조

신규 ECS 모듈 추가 시:

1. `xyz.anythings.{new_ecs}` 패키지 생성
2. 엔티티, 서비스, 컨트롤러 구현
3. `@ComponentScan`에 패키지 자동 포함
4. RabbitMQ 큐 정의 및 메시지 모델 추가

#### 8.1.2 이벤트 기반 확장

Spring Event를 통해 모듈 간 느슨한 결합:

```java
// 이벤트 발행
applicationEventPublisher.publishEvent(new IndicatorOnEvent(...));

// 이벤트 구독
@EventListener
public void handleIndicatorOnEvent(IndicatorOnEvent event) {
    // 처리 로직
}
```

#### 8.1.3 동적 서비스 확장

Groovy/JRuby 스크립트를 통한 런타임 로직 확장 가능.

### 8.2 설계 원칙

| 원칙 | 적용 방법 |
|------|-----------|
| **단일 책임** | 레이어별, 패키지별 명확한 역할 분리 |
| **개방-폐쇄** | 인터페이스 기반 설계, 이벤트 기반 확장 |
| **의존성 역전** | 서비스 인터페이스(`api` 패키지) 정의 후 구현 |
| **모듈화** | 기능별 패키지 분리, 독립적 배포 가능 구조 |
| **이벤트 기반** | Spring Event, RabbitMQ를 통한 비동기 처리 |

### 8.3 성능 최적화

| 기법 | 설명 |
|------|------|
| **캐시** | Redis 기반 분산 캐시, 로컬 캐시 병행 |
| **비동기 처리** | `@Async`, RabbitMQ를 통한 비동기 작업 |
| **커넥션 풀** | DBCP, HikariCP를 통한 DB 커넥션 풀링 |
| **배치 처리** | Quartz 스케줄러를 통한 대량 데이터 처리 |
| **인덱싱** | Elasticsearch를 통한 빠른 검색 |

---

## 9. 보안

### 9.1 인증

| 방식 | 설명 |
|------|------|
| **세션 기반** | Redis/JDBC 기반 세션 관리 |
| **JWT 토큰** | 모바일 단말(PDA) 등 Stateless 인증 |

### 9.2 인가

| 방식 | 설명 |
|------|------|
| **역할(Role) 기반** | 관리자, 작업자, 모니터링 등 역할별 권한 |
| **권한(Permission) 기반** | 메뉴, 기능, API 엔드포인트별 세밀한 권한 제어 |

### 9.3 데이터 암호화

| 대상 | 방법 |
|------|------|
| **설정 파일** | Jasypt를 통한 민감 정보 암호화 |
| **통신** | HTTPS (운영 환경) |
| **패스워드** | BCrypt 해싱 |

---

## 10. 에러 처리 및 로깅

### 10.1 예외 처리

**패키지**: `xyz.elidom.exception`

| 예외 타입 | 설명 |
|-----------|------|
| `ClientException` | 클라이언트 요청 오류 (400번대) |
| `ServerException` | 서버 내부 오류 (500번대) |

**전역 예외 핸들러**: `@ControllerAdvice`를 통한 중앙 집중식 예외 처리

### 10.2 로깅

| 로그 유형 | 저장 위치 | 용도 |
|-----------|-----------|------|
| **애플리케이션 로그** | 파일, 콘솔 | 디버깅, 운영 모니터링 |
| **RabbitMQ 메시지 로그** | Elasticsearch | 메시지 추적, 설비 통신 이력 |
| **작업 이력** | RDBMS | 작업 처리 이력, 실적 집계 |

---

## 11. 배포 아키텍처

### 11.1 개발 환경

```
┌─────────────────────────────────────────────────┐
│           Spring Boot 내장 웹 서버               │
│  - API 서버 (포트 8080)                          │
│  - 정적 파일 서빙 (src/main/resources/static/)   │
└─────────────────────────────────────────────────┘
```

### 11.2 운영 환경 (Docker)

```
┌──────────────────────────────────────────────────────┐
│                  Docker Compose                      │
│                                                      │
│  ┌────────────────────┐       ┌──────────────────┐  │
│  │   Nginx Container  │       │  App Container   │  │
│  │  - 정적 파일 서빙   │◀─────→│  Spring Boot JAR │  │
│  │  - API 프록시       │       │  (포트 8080)      │  │
│  │  (포트 80/443)      │       └──────────────────┘  │
│  └────────────────────┘                              │
│                                                      │
│  ┌──────────────────┐  ┌──────────────────┐         │
│  │  MySQL Container │  │ Redis Container  │         │
│  └──────────────────┘  └──────────────────┘         │
│                                                      │
│  ┌──────────────────┐  ┌──────────────────┐         │
│  │ RabbitMQ         │  │ Elasticsearch    │         │
│  │ Container        │  │ Container        │         │
│  └──────────────────┘  └──────────────────┘         │
└──────────────────────────────────────────────────────┘
```

---

## 12. 참고 문서

| 문서 | 경로 |
|------|------|
| CLAUDE.md | `/CLAUDE.md` |
| Docker 배포 가이드 | `/docs/DOCKER.md` |
| ECS 모듈 기능 정의 | `/docs/requirements/ECS-모듈-기능정의.md` |
| 프로젝트 구조 | `/CLAUDE.md#프로젝트-구조` |

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2026-03-01 | 초기 작성 | Claude Code |

---

**문서 끝**
