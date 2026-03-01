# Operato WCS 프로젝트 WBS (Work Breakdown Structure)

## 문서 정보

- **프로젝트**: Operato WCS (Warehouse Control System)
- **버전**: 1.0.0
- **작성일**: 2026-03-01
- **작성자**: Claude Sonnet 4.5
- **기준 계획서**: [operato-wcs-development-plan.md](operato-wcs-development-plan.md)

---

## WBS 범례

### 역할 코드

| 코드 | 역할 |
|------|------|
| BE | Backend Developer (백엔드 개발자) |
| FE | Frontend Developer (프론트엔드 개발자) |
| FS | Full-stack Developer (풀스택 개발자) |
| DO | DevOps Engineer (DevOps 엔지니어) |
| QA | QA Engineer (QA 엔지니어) |
| PM | Project Manager (프로젝트 매니저) |

### 진행률 표시

- 🟢 완료 (100%)
- 🟡 진행 중 (1-99%)
- ⚪ 미시작 (0%)
- 🔴 지연

---

## Phase 1: 코어 시스템 구축 (Week 1-8)

### 1.1 프로젝트 초기 설정 (Week 1)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 1.1.1 | BE | 백엔드 초기화 | Spring Boot 3.4 프로젝트 생성 및 기본 설정 | Week 1-Day 1 | ⚪ | 0% | build.gradle, settings.gradle |
| 1.1.2 | BE | 백엔드 구조 | Gradle 멀티 모듈 구조 설정 (wcs-common, wcs-domain, wcs-api) | Week 1-Day 2 | ⚪ | 0% | 모듈 의존성 정의 |
| 1.1.3 | BE | 공통 라이브러리 | 공통 유틸리티, 예외 처리, 응답 포맷 구현 | Week 1-Day 3 | ⚪ | 0% | wcs-common 모듈 |
| 1.1.4 | FE | 프론트엔드 초기화 | Lit + Vite + TypeScript 프로젝트 초기화 | Week 1-Day 1 | ⚪ | 0% | client/ 디렉토리 |
| 1.1.5 | FE | 디자인 시스템 | CSS Custom Properties 정의 (색상, 폰트, 간격 등) | Week 1-Day 2 | ⚪ | 0% | styles/variables.css |
| 1.1.6 | FE | 컴포넌트 구조 | 공통 컴포넌트 라이브러리 구조 설정 | Week 1-Day 3 | ⚪ | 0% | components/ 디렉토리 |
| 1.1.7 | FE | 라우터 설정 | 클라이언트 사이드 라우터 설정 (Vaadin Router) | Week 1-Day 4 | ⚪ | 0% | router/ 디렉토리 |
| 1.1.8 | DO | 인프라 구성 | Docker Compose 환경 구성 (PostgreSQL, RabbitMQ, Redis) | Week 1-Day 2 | ⚪ | 0% | docker-compose.yml |
| 1.1.9 | DO | CI/CD 초기 구성 | GitHub Actions 워크플로우 생성 (빌드, 테스트) | Week 1-Day 4 | ⚪ | 0% | .github/workflows/ |
| 1.1.10 | PM | 개발 환경 문서화 | 개발 환경 설정 가이드 작성 | Week 1-Day 5 | ⚪ | 0% | docs/setup.md |

### 1.2 인증·인가 시스템 (Week 2)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 1.2.1 | BE | 인증 구현 | JWT 기반 인증 구현 (토큰 생성, 검증) | Week 2-Day 1-2 | ⚪ | 0% | JwtTokenProvider 클래스 |
| 1.2.2 | BE | Spring Security | Spring Security 설정 (필터, 핸들러) | Week 2-Day 2-3 | ⚪ | 0% | SecurityConfig 클래스 |
| 1.2.3 | BE | 사용자 관리 API | 사용자 CRUD API 구현 | Week 2-Day 3 | ⚪ | 0% | /api/users |
| 1.2.4 | BE | 권한 관리 API | 역할·권한 관리 API 구현 | Week 2-Day 4 | ⚪ | 0% | /api/roles |
| 1.2.5 | BE | 세션 관리 | Redis 기반 세션 관리 구현 | Week 2-Day 4 | ⚪ | 0% | Spring Session Redis |
| 1.2.6 | FE | 로그인 UI | Manager 로그인 화면 구현 | Week 2-Day 3 | ⚪ | 0% | pages/login/ |
| 1.2.7 | FE | 인증 서비스 | JWT 토큰 관리 및 인증 상태 관리 | Week 2-Day 4 | ⚪ | 0% | services/auth/ |
| 1.2.8 | QA | 인증 테스트 | 로그인, 토큰 갱신, 권한 검증 테스트 | Week 2-Day 5 | ⚪ | 0% | JUnit, Postman |

### 1.3 마스터 데이터 관리 (Week 3-4)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 1.3.1 | BE | DB 스키마 | 상품 마스터 테이블 설계 및 생성 | Week 3-Day 1 | ⚪ | 0% | products 테이블, ERD |
| 1.3.2 | BE | 상품 엔티티 | Product 엔티티 및 Repository 구현 | Week 3-Day 1 | ⚪ | 0% | JPA Entity |
| 1.3.3 | BE | 상품 API | 상품 CRUD API 구현 | Week 3-Day 2 | ⚪ | 0% | /api/products |
| 1.3.4 | BE | 상품 카테고리 | 상품 카테고리 관리 API 구현 | Week 3-Day 3 | ⚪ | 0% | /api/categories |
| 1.3.5 | BE | 바코드/SKU | 바코드·SKU 관리 로직 구현 | Week 3-Day 3 | ⚪ | 0% | 중복 검증 포함 |
| 1.3.6 | BE | 설비 마스터 | 설비 마스터 테이블 설계 및 엔티티 구현 | Week 3-Day 4 | ⚪ | 0% | equipments 테이블 |
| 1.3.7 | BE | 설비 API | 설비 CRUD 및 상태 관리 API 구현 | Week 3-Day 5 | ⚪ | 0% | /api/equipments |
| 1.3.8 | BE | 작업자 마스터 | 작업자 마스터 테이블 설계 및 엔티티 구현 | Week 4-Day 1 | ⚪ | 0% | workers 테이블 |
| 1.3.9 | BE | 작업자 API | 작업자 CRUD, 스킬, 출퇴근 관리 API | Week 4-Day 2 | ⚪ | 0% | /api/workers |
| 1.3.10 | FE | 상품 관리 UI | 상품 목록, 등록, 수정 화면 구현 | Week 3-Day 3-4 | ⚪ | 0% | pages/product/ |
| 1.3.11 | FE | 설비 관리 UI | 설비 목록, 등록, 상태 변경 화면 구현 | Week 4-Day 1-2 | ⚪ | 0% | pages/equipment/ |
| 1.3.12 | FE | 작업자 관리 UI | 작업자 목록, 등록, 출퇴근 기록 화면 | Week 4-Day 3 | ⚪ | 0% | pages/worker/ |
| 1.3.13 | QA | 마스터 API 테스트 | 상품, 설비, 작업자 CRUD 테스트 | Week 4-Day 4-5 | ⚪ | 0% | Postman Collection |

### 1.4 주문 수신 및 분배 시스템 (Week 5-6)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 1.4.1 | BE | DB 스키마 | 주문 테이블 설계 및 생성 | Week 5-Day 1 | ⚪ | 0% | orders, order_items 테이블 |
| 1.4.2 | BE | 주문 엔티티 | Order, OrderItem 엔티티 및 Repository 구현 | Week 5-Day 1 | ⚪ | 0% | JPA Entity, 연관관계 |
| 1.4.3 | BE | 주문 수신 API | WMS 주문 수신 API 구현 | Week 5-Day 2 | ⚪ | 0% | POST /api/orders/receive |
| 1.4.4 | BE | 주문 검증 | 주문 데이터 검증 로직 구현 | Week 5-Day 2 | ⚪ | 0% | 상품, 수량 검증 |
| 1.4.5 | BE | 주문 상태 관리 | 주문 상태 전이 로직 구현 | Week 5-Day 3 | ⚪ | 0% | 수신→분배→처리→완료 |
| 1.4.6 | BE | 주문 분석 엔진 | 주문 특성 분석 알고리즘 구현 | Week 5-Day 4 | ⚪ | 0% | 주문 유형, 긴급도 분석 |
| 1.4.7 | BE | 설비 Capa 관리 | 설비 용량(Capa) 관리 및 계산 로직 | Week 5-Day 5 | ⚪ | 0% | 가용 용량, 예약 용량 |
| 1.4.8 | BE | 분배 알고리즘 | 설비 Capa 기반 주문 분배 알고리즘 구현 | Week 6-Day 1-2 | ⚪ | 0% | 최적 설비 선택 |
| 1.4.9 | BE | 분배 API | 주문 분배 API 구현 | Week 6-Day 3 | ⚪ | 0% | POST /api/orders/{id}/distribute |
| 1.4.10 | BE | 분배 이력 | 주문-설비 매핑 및 분배 이력 관리 | Week 6-Day 3 | ⚪ | 0% | order_assignments 테이블 |
| 1.4.11 | FE | 주문 목록 UI | 주문 목록, 필터링, 정렬 화면 | Week 6-Day 2-3 | ⚪ | 0% | pages/order/ |
| 1.4.12 | FE | 주문 상세 UI | 주문 상세 조회 및 분배 현황 화면 | Week 6-Day 4 | ⚪ | 0% | pages/order/detail/ |
| 1.4.13 | QA | 주문 처리 테스트 | 주문 수신, 검증, 분배 프로세스 테스트 | Week 6-Day 5 | ⚪ | 0% | E2E 시나리오 |

### 1.5 실적 집계 및 보고 시스템 (Week 7)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 1.5.1 | BE | DB 스키마 | 실적 테이블 설계 및 생성 | Week 7-Day 1 | ⚪ | 0% | work_results 테이블 |
| 1.5.2 | BE | 실적 엔티티 | WorkResult 엔티티 및 Repository 구현 | Week 7-Day 1 | ⚪ | 0% | JPA Entity |
| 1.5.3 | BE | 실적 수신 API | ECS 실적 수신 API 구현 | Week 7-Day 2 | ⚪ | 0% | POST /api/results |
| 1.5.4 | BE | 실적 검증 | 실적 데이터 검증 및 집계 로직 | Week 7-Day 2 | ⚪ | 0% | 수량, 시간 검증 |
| 1.5.5 | BE | 실적 집계 | 설비별, 주문별 실적 집계 로직 | Week 7-Day 3 | ⚪ | 0% | SUM, COUNT, AVG |
| 1.5.6 | BE | 실적 보고 API | WMS 실적 전송 API 구현 | Week 7-Day 4 | ⚪ | 0% | POST /api/results/report |
| 1.5.7 | BE | 실적 보고서 | 실적 보고서 생성 로직 (JSON, Excel) | Week 7-Day 4 | ⚪ | 0% | Apache POI |
| 1.5.8 | FE | 실적 조회 UI | 실적 목록, 필터링 화면 | Week 7-Day 3 | ⚪ | 0% | pages/result/ |
| 1.5.9 | FE | 실적 대시보드 | 실적 대시보드 (차트, 그래프) | Week 7-Day 5 | ⚪ | 0% | Chart.js 또는 Lit 차트 |
| 1.5.10 | QA | 실적 처리 테스트 | 실적 수신, 집계, 보고 프로세스 테스트 | Week 7-Day 5 | ⚪ | 0% | 통합 테스트 |

### 1.6 RabbitMQ 메시지 인프라 (Week 8)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 1.6.1 | BE | RabbitMQ 설정 | Exchange, Queue, Routing Key 정의 | Week 8-Day 1 | ⚪ | 0% | rabbitmq.conf |
| 1.6.2 | BE | 메시지 스키마 | 메시지 JSON 스키마 정의 (Command, Status, Result) | Week 8-Day 1 | ⚪ | 0% | docs/message-schema.md |
| 1.6.3 | BE | 메시지 발행 | 메시지 발행 공통 라이브러리 구현 | Week 8-Day 2 | ⚪ | 0% | MessagePublisher 클래스 |
| 1.6.4 | BE | 메시지 구독 | 메시지 구독 공통 라이브러리 구현 | Week 8-Day 2 | ⚪ | 0% | MessageConsumer 클래스 |
| 1.6.5 | BE | 메시지 이력 | 메시지 이력 로깅 및 저장 로직 | Week 8-Day 3 | ⚪ | 0% | message_logs 테이블 |
| 1.6.6 | BE | 에러 처리 | Dead Letter Queue 설정 및 에러 처리 | Week 8-Day 4 | ⚪ | 0% | DLQ, Retry 로직 |
| 1.6.7 | DO | RabbitMQ 모니터링 | RabbitMQ Management Plugin 설정 | Week 8-Day 3 | ⚪ | 0% | http://localhost:15672 |
| 1.6.8 | QA | 메시지 테스트 | 메시지 발행·구독 통합 테스트 | Week 8-Day 5 | ⚪ | 0% | 연결, 전송, 수신 검증 |

### Phase 1 완료 기준 체크리스트

| ID | 완료 기준 | 담당 | 완료 여부 |
|----|----------|------|----------|
| P1-C1 | 인증·인가 시스템 동작 (로그인, JWT 검증) | BE, FE | ⚪ |
| P1-C2 | 마스터 데이터 CRUD API 완성 (상품, 설비, 작업자) | BE | ⚪ |
| P1-C3 | 주문 수신 및 분배 프로세스 동작 | BE | ⚪ |
| P1-C4 | 실적 수집 및 보고 프로세스 동작 | BE | ⚪ |
| P1-C5 | RabbitMQ 메시지 발행·구독 정상 동작 | BE | ⚪ |
| P1-C6 | 테스트 커버리지 60% 이상 (핵심 비즈니스 로직) | BE, QA | ⚪ |

---

## Phase 2: 설비 모듈 개발 (Week 9-20)

### 2.1 설비 플러그인 SPI 설계 (Week 9)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 2.1.1 | BE | SPI 인터페이스 | EquipmentPlugin 인터페이스 설계 | Week 9-Day 1 | ⚪ | 0% | initialize(), start(), stop() |
| 2.1.2 | BE | 생명주기 관리 | 설비 플러그인 생명주기 관리 구현 | Week 9-Day 2 | ⚪ | 0% | PluginManager 클래스 |
| 2.1.3 | BE | 상태 모니터링 | 설비 상태 모니터링 인터페이스 정의 | Week 9-Day 2 | ⚪ | 0% | getStatus() 메서드 |
| 2.1.4 | BE | 작업 할당 | 작업 할당 인터페이스 정의 | Week 9-Day 3 | ⚪ | 0% | assignWork(), cancelWork() |
| 2.1.5 | BE | 실적 보고 | 실적 보고 인터페이스 정의 | Week 9-Day 3 | ⚪ | 0% | getResults() 메서드 |
| 2.1.6 | BE | 플러그인 로더 | 동적 플러그인 로딩·언로딩 구현 | Week 9-Day 4 | ⚪ | 0% | ServiceLoader 활용 |
| 2.1.7 | BE | 설정 관리 | 설비별 설정 관리 시스템 구현 | Week 9-Day 5 | ⚪ | 0% | EquipmentConfig 클래스 |
| 2.1.8 | PM | SPI 문서화 | 플러그인 개발 가이드 작성 | Week 9-Day 5 | ⚪ | 0% | docs/plugin-guide.md |

### 2.2 입고 PDA 모듈 (Week 10-11)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 2.2.1 | BE | PDA 등록 | PDA 단말 등록 및 관리 API | Week 10-Day 1 | ⚪ | 0% | pda_devices 테이블 |
| 2.2.2 | BE | 입고 주문 조회 | 입고 대기 주문 조회 API | Week 10-Day 2 | ⚪ | 0% | GET /api/inbound/pda/orders |
| 2.2.3 | BE | 바코드 스캔 | 바코드 스캔 처리 API | Week 10-Day 3 | ⚪ | 0% | POST /api/inbound/pda/scan |
| 2.2.4 | BE | 입고 검수 | 입고 검수 로직 구현 | Week 10-Day 3 | ⚪ | 0% | 수량, 품질 검증 |
| 2.2.5 | BE | 로케이션 할당 | 로케이션 자동 할당 알고리즘 | Week 10-Day 4 | ⚪ | 0% | 최적 위치 선정 |
| 2.2.6 | BE | 입고 처리 | 입고 처리 및 실적 보고 API | Week 10-Day 5 | ⚪ | 0% | POST /api/inbound/pda/putaway |
| 2.2.7 | FE | PDA 로그인 | PDA 로그인 화면 (모바일) | Week 11-Day 1 | ⚪ | 0% | 바코드 스캔 로그인 |
| 2.2.8 | FE | 입고 주문 목록 | 입고 대기 주문 목록 화면 | Week 11-Day 2 | ⚪ | 0% | PWA 최적화 |
| 2.2.9 | FE | 바코드 스캔 UI | 바코드 스캔 화면 (카메라 연동) | Week 11-Day 3 | ⚪ | 0% | ZXing 라이브러리 |
| 2.2.10 | FE | 입고 완료 UI | 입고 처리 및 완료 확인 화면 | Week 11-Day 4 | ⚪ | 0% | 진행 상황 표시 |
| 2.2.11 | QA | PDA 모듈 테스트 | 입고 PDA 워크플로우 테스트 | Week 11-Day 5 | ⚪ | 0% | 실제 모바일 단말 |

### 2.3 Put-To-Light 모듈 (Week 12-13)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 2.3.1 | BE | 셀 관리 | 셀 마스터 관리 및 API | Week 12-Day 1 | ⚪ | 0% | cells 테이블 |
| 2.3.2 | BE | 셀-주문 매핑 | 셀-주문 매핑 로직 및 API | Week 12-Day 2 | ⚪ | 0% | POST /api/ptl/mapping |
| 2.3.3 | BE | 상품 스캔 | 상품 스캔 처리 API | Week 12-Day 3 | ⚪ | 0% | POST /api/ptl/scan |
| 2.3.4 | BE | 표시기 제어 | RabbitMQ 표시기 점등·소등 메시지 발행 | Week 12-Day 4 | ⚪ | 0% | wcs.command.indicator.on |
| 2.3.5 | BE | 확인 처리 | 작업자 확인 버튼 처리 로직 | Week 12-Day 5 | ⚪ | 0% | wcs.status.indicator.confirm |
| 2.3.6 | BE | 작업 완료 | 작업 완료 감지 및 실적 보고 | Week 13-Day 1 | ⚪ | 0% | 모든 셀 완료 시 |
| 2.3.7 | FE | PTL 현황 UI | 셀 배치도 및 작업 현황 화면 | Week 13-Day 2 | ⚪ | 0% | 실시간 상태 표시 |
| 2.3.8 | FE | 매핑 관리 UI | 셀-주문 매핑 관리 화면 | Week 13-Day 3 | ⚪ | 0% | Drag & Drop |
| 2.3.9 | FE | 작업 진행 UI | 작업 진행 상황 모니터링 화면 | Week 13-Day 4 | ⚪ | 0% | WebSocket 실시간 |
| 2.3.10 | QA | PTL 모듈 테스트 | Put-To-Light 워크플로우 테스트 | Week 13-Day 5 | ⚪ | 0% | 표시기 연동 |

### 2.4 Pick-To-Light 모듈 (Week 14-15)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 2.4.1 | BE | 셀-상품 배치 | 셀-상품 배치 관리 API | Week 14-Day 1 | ⚪ | 0% | POST /api/pkl/placement |
| 2.4.2 | BE | 박스 스캔 | 박스(주문) 스캔 처리 API | Week 14-Day 2 | ⚪ | 0% | POST /api/pkl/scan/box |
| 2.4.3 | BE | 피킹 셀 점등 | 피킹 대상 셀 표시기 점등 로직 | Week 14-Day 3 | ⚪ | 0% | RabbitMQ 메시지 |
| 2.4.4 | BE | 수량 확인 | 피킹 수량 확인 및 검증 | Week 14-Day 4 | ⚪ | 0% | POST /api/pkl/confirm |
| 2.4.5 | BE | 작업 완료 | 피킹 완료 처리 및 실적 보고 | Week 14-Day 5 | ⚪ | 0% | 박스 단위 완료 |
| 2.4.6 | FE | 셀 배치 UI | 셀-상품 배치 관리 화면 | Week 15-Day 1 | ⚪ | 0% | 그리드 레이아웃 |
| 2.4.7 | FE | 피킹 현황 UI | 피킹 작업 현황 모니터링 | Week 15-Day 2 | ⚪ | 0% | 실시간 갱신 |
| 2.4.8 | FE | 작업자 UI | 작업자용 피킹 안내 화면 | Week 15-Day 3 | ⚪ | 0% | 태블릿 최적화 |
| 2.4.9 | QA | P-TL 모듈 테스트 | Pick-To-Light 워크플로우 테스트 | Week 15-Day 4-5 | ⚪ | 0% | 표시기 연동 |

### 2.5 P-DAS 모듈 (Week 16-17)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 2.5.1 | BE | 피킹 주문 조회 | 피킹 대기 주문 조회 API | Week 16-Day 1 | ⚪ | 0% | GET /api/pdas/orders |
| 2.5.2 | BE | 작업자 할당 | 작업자 자동 할당 알고리즘 | Week 16-Day 2 | ⚪ | 0% | POST /api/pdas/assign |
| 2.5.3 | BE | 피킹 경로 | 피킹 경로 최적화 알고리즘 | Week 16-Day 3-4 | ⚪ | 0% | TSP 또는 Greedy |
| 2.5.4 | BE | 피킹 리스트 | 피킹 리스트 생성 API | Week 16-Day 5 | ⚪ | 0% | GET /api/pdas/picklist/{id} |
| 2.5.5 | BE | 피킹 처리 | 피킹 처리 API (바코드 스캔) | Week 17-Day 1 | ⚪ | 0% | POST /api/pdas/pick |
| 2.5.6 | BE | 피킹 완료 | 피킹 완료 및 실적 보고 API | Week 17-Day 2 | ⚪ | 0% | POST /api/pdas/complete |
| 2.5.7 | FE | P-DAS 주문 목록 | 피킹 주문 목록 화면 (PDA) | Week 17-Day 1 | ⚪ | 0% | 모바일 최적화 |
| 2.5.8 | FE | 피킹 리스트 UI | 피킹 리스트 화면 (경로 표시) | Week 17-Day 2 | ⚪ | 0% | 지도형 UI |
| 2.5.9 | FE | 피킹 진행 UI | 피킹 진행 상황 화면 | Week 17-Day 3 | ⚪ | 0% | 진행률 표시 |
| 2.5.10 | QA | P-DAS 모듈 테스트 | P-DAS 워크플로우 테스트 | Week 17-Day 4-5 | ⚪ | 0% | PDA 단말 |

### 2.6 SMS (Sorter Management System) 모듈 (Week 18-20)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 2.6.1 | BE | Sorter 연동 | Sorter 설비 RabbitMQ 연동 구현 | Week 18-Day 1 | ⚪ | 0% | wcs.command.sorter.* |
| 2.6.2 | BE | 슈트 관리 | 슈트(Chute) 마스터 관리 API | Week 18-Day 2 | ⚪ | 0% | chutes 테이블 |
| 2.6.3 | BE | 슈트-주문 매핑 | 슈트-주문 매핑 로직 및 API | Week 18-Day 3 | ⚪ | 0% | POST /api/sms/mapping |
| 2.6.4 | BE | 투입 명령 | Sorter 투입 명령 메시지 발행 | Week 18-Day 4 | ⚪ | 0% | wcs.command.sorter.induct |
| 2.6.5 | BE | 분류 명령 | Sorter 분류 명령 메시지 발행 | Week 18-Day 5 | ⚪ | 0% | wcs.command.sorter.divert |
| 2.6.6 | BE | 투입 완료 처리 | 투입 완료 메시지 수신 및 처리 | Week 19-Day 1 | ⚪ | 0% | wcs.status.sorter.inducted |
| 2.6.7 | BE | 분류 완료 처리 | 분류 완료 메시지 수신 및 처리 | Week 19-Day 1 | ⚪ | 0% | wcs.status.sorter.diverted |
| 2.6.8 | BE | No-Read 처리 | No-Read 처리 로직 및 API | Week 19-Day 2 | ⚪ | 0% | POST /api/sms/noread |
| 2.6.9 | BE | 실적 보고 | SMS 실적 집계 및 보고 로직 | Week 19-Day 3 | ⚪ | 0% | 분류 완료 실적 |
| 2.6.10 | FE | Sorter 모니터링 | Sorter 실시간 모니터링 화면 | Week 19-Day 4 | ⚪ | 0% | 슈트 상태 표시 |
| 2.6.11 | FE | 슈트 매핑 UI | 슈트-주문 매핑 관리 화면 | Week 19-Day 5 | ⚪ | 0% | Drag & Drop |
| 2.6.12 | FE | 분류 현황 UI | 분류 진행 현황 대시보드 | Week 20-Day 1 | ⚪ | 0% | 실시간 차트 |
| 2.6.13 | FE | No-Read 관리 UI | No-Read 처리 및 재투입 화면 | Week 20-Day 2 | ⚪ | 0% | 예외 처리 |
| 2.6.14 | QA | SMS 모듈 테스트 | SMS 워크플로우 통합 테스트 | Week 20-Day 3-5 | ⚪ | 0% | Sorter 연동 |

### Phase 2 완료 기준 체크리스트

| ID | 완료 기준 | 담당 | 완료 여부 |
|----|----------|------|----------|
| P2-C1 | 5개 설비 모듈 모두 플러그인으로 동작 | BE | ⚪ |
| P2-C2 | RabbitMQ 메시지 기반 설비 제어 정상 동작 | BE | ⚪ |
| P2-C3 | 각 모듈별 실적 보고 정상 동작 | BE | ⚪ |
| P2-C4 | 설비 플러그인 동적 로딩·언로딩 가능 | BE | ⚪ |
| P2-C5 | 테스트 커버리지 70% 이상 | BE, QA | ⚪ |

---

## Phase 3: 클라이언트 UI 개발 (Week 21-30)

### 3.1 디자인 시스템 구축 (Week 21-22)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 3.1.1 | FE | 기본 컴포넌트 | Button, Input, Checkbox 등 기본 컴포넌트 | Week 21-Day 1-2 | ⚪ | 0% | components/common/ |
| 3.1.2 | FE | 폼 컴포넌트 | Select, Textarea, Radio 컴포넌트 | Week 21-Day 3 | ⚪ | 0% | 폼 검증 포함 |
| 3.1.3 | FE | 데이터 컴포넌트 | Table, Pagination, DataGrid 컴포넌트 | Week 21-Day 4-5 | ⚪ | 0% | 정렬, 필터링 |
| 3.1.4 | FE | 피드백 컴포넌트 | Modal, Toast, Alert, Confirm 컴포넌트 | Week 22-Day 1 | ⚪ | 0% | 애니메이션 |
| 3.1.5 | FE | 네비게이션 | Tab, Accordion, Dropdown 컴포넌트 | Week 22-Day 2 | ⚪ | 0% | 접근성 고려 |
| 3.1.6 | FE | 상태 표시 | Badge, Tag, Chip, Progress Bar 컴포넌트 | Week 22-Day 3 | ⚪ | 0% | 색상 테마 |
| 3.1.7 | FE | 레이아웃 | Header, Sidebar, Footer 레이아웃 컴포넌트 | Week 22-Day 4 | ⚪ | 0% | Responsive |
| 3.1.8 | FE | 테마 시스템 | Light/Dark 테마 지원 구현 | Week 22-Day 5 | ⚪ | 0% | CSS Variables |

### 3.2 Manager UI (Week 23-25)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 3.2.1 | FE | 대시보드 레이아웃 | 대시보드 레이아웃 및 위젯 시스템 | Week 23-Day 1 | ⚪ | 0% | 그리드 레이아웃 |
| 3.2.2 | FE | 주문 현황 위젯 | 주문 현황 요약 위젯 | Week 23-Day 2 | ⚪ | 0% | 금일 입고/출고/완료 |
| 3.2.3 | FE | 설비 현황 위젯 | 설비 상태 요약 위젯 | Week 23-Day 3 | ⚪ | 0% | 가동/점검/오류 |
| 3.2.4 | FE | 작업자 현황 위젯 | 작업자 현황 위젯 | Week 23-Day 4 | ⚪ | 0% | 출근/작업 중/휴게 |
| 3.2.5 | FE | 실시간 알림 | 실시간 알림 시스템 (WebSocket) | Week 23-Day 5 | ⚪ | 0% | 오류, 경고, 완료 |
| 3.2.6 | FE | 주문 목록 | 주문 목록 화면 (필터, 정렬, 검색) | Week 24-Day 1 | ⚪ | 0% | DataGrid 활용 |
| 3.2.7 | FE | 주문 상세 | 주문 상세 조회 및 분배 현황 | Week 24-Day 2 | ⚪ | 0% | 타임라인 UI |
| 3.2.8 | FE | 주문 수동 분배 | 주문 수동 분배 화면 | Week 24-Day 3 | ⚪ | 0% | 설비 선택 UI |
| 3.2.9 | FE | 설비 목록 | 설비 목록 및 상태 관리 화면 | Week 24-Day 4 | ⚪ | 0% | 카드 레이아웃 |
| 3.2.10 | FE | 설비 모니터링 | 설비 실시간 모니터링 화면 | Week 24-Day 5 | ⚪ | 0% | WebSocket 연동 |
| 3.2.11 | FE | 마스터 관리 | 상품·작업자·설비 관리 통합 화면 | Week 25-Day 1-2 | ⚪ | 0% | CRUD 통합 |
| 3.2.12 | FE | 실적 조회 | 실적 조회 화면 (일별, 월별) | Week 25-Day 3 | ⚪ | 0% | 날짜 필터 |
| 3.2.13 | FE | 실적 차트 | 실적 대시보드 (차트, 그래프) | Week 25-Day 4 | ⚪ | 0% | Chart.js |
| 3.2.14 | FE | 보고서 다운로드 | 실적 보고서 다운로드 (Excel, PDF) | Week 25-Day 5 | ⚪ | 0% | API 연동 |

### 3.3 Kiosk UI (Week 26)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 3.3.1 | FE | Kiosk 로그인 | 작업자 로그인 화면 (바코드 스캔) | Week 26-Day 1 | ⚪ | 0% | 카메라 연동 |
| 3.3.2 | FE | 출퇴근 처리 | 출근·퇴근 처리 화면 | Week 26-Day 2 | ⚪ | 0% | 시간 기록 |
| 3.3.3 | FE | 작업 지시 확인 | 작업 지시 확인 화면 | Week 26-Day 3 | ⚪ | 0% | 할당된 작업 |
| 3.3.4 | FE | 휴게 관리 | 휴게 시간 관리 화면 | Week 26-Day 4 | ⚪ | 0% | 타이머 |
| 3.3.5 | QA | Kiosk UI 테스트 | Kiosk 화면 통합 테스트 | Week 26-Day 5 | ⚪ | 0% | 터치 스크린 |

### 3.4 PDA UI (Week 27-28)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 3.4.1 | FE | PDA 공통 레이아웃 | PDA 공통 레이아웃 및 네비게이션 | Week 27-Day 1 | ⚪ | 0% | 모바일 최적화 |
| 3.4.2 | FE | 입고 PDA UI | 입고 PDA 화면 (앞서 구현한 것 개선) | Week 27-Day 2 | ⚪ | 0% | UX 개선 |
| 3.4.3 | FE | P-DAS PDA UI | P-DAS PDA 화면 (앞서 구현한 것 개선) | Week 27-Day 3 | ⚪ | 0% | UX 개선 |
| 3.4.4 | FE | 작업 이력 | 작업 이력 조회 화면 | Week 27-Day 4 | ⚪ | 0% | 무한 스크롤 |
| 3.4.5 | FE | PDA 설정 | PDA 설정 화면 (언어, 알림 등) | Week 27-Day 5 | ⚪ | 0% | Local Storage |
| 3.4.6 | FE | 오프라인 모드 | 오프라인 모드 지원 (PWA) | Week 28-Day 1-2 | ⚪ | 0% | Service Worker |
| 3.4.7 | FE | 푸시 알림 | 푸시 알림 기능 구현 | Week 28-Day 3 | ⚪ | 0% | Web Push API |
| 3.4.8 | QA | PDA UI 테스트 | PDA 화면 통합 테스트 | Week 28-Day 4-5 | ⚪ | 0% | 모바일 단말 |

### 3.5 Monitoring Board UI (Week 29-30)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 3.5.1 | FE | 모니터링 레이아웃 | 대형 화면용 레이아웃 (Full Screen) | Week 29-Day 1 | ⚪ | 0% | TV/모니터 최적화 |
| 3.5.2 | FE | 설비 상태 맵 | 설비 상태 맵 (전체 설비 한눈에) | Week 29-Day 2 | ⚪ | 0% | SVG 또는 Canvas |
| 3.5.3 | FE | 주문 진행 현황 | 주문 진행 현황 (파이 차트, 바 차트) | Week 29-Day 3 | ⚪ | 0% | 애니메이션 차트 |
| 3.5.4 | FE | 작업자 현황 | 작업자 현황 (출근 인원, 작업 중) | Week 29-Day 4 | ⚪ | 0% | 실시간 갱신 |
| 3.5.5 | FE | 실시간 알림 | 실시간 알림 표시 (오류, 경고) | Week 29-Day 5 | ⚪ | 0% | 팝업 알림 |
| 3.5.6 | FE | WebSocket 연동 | WebSocket 기반 실시간 데이터 갱신 | Week 30-Day 1 | ⚪ | 0% | 자동 재연결 |
| 3.5.7 | FE | 3D 재고 모니터링 | 3D 재고 모니터링 (선택 사항) | Week 30-Day 2-3 | ⚪ | 0% | Three.js |
| 3.5.8 | FE | 자동 갱신 | 데이터 자동 갱신 및 캐싱 | Week 30-Day 4 | ⚪ | 0% | 5초 간격 |
| 3.5.9 | QA | 모니터링 UI 테스트 | 모니터링 보드 통합 테스트 | Week 30-Day 5 | ⚪ | 0% | 대형 화면 |

### Phase 3 완료 기준 체크리스트

| ID | 완료 기준 | 담당 | 완료 여부 |
|----|----------|------|----------|
| P3-C1 | Manager UI 완성 (대시보드, 주문·설비·마스터·실적 관리) | FE | ⚪ |
| P3-C2 | Kiosk UI 완성 (작업자 로그인, 출퇴근) | FE | ⚪ |
| P3-C3 | PDA UI 완성 (입고, P-DAS) | FE | ⚪ |
| P3-C4 | Monitoring Board UI 완성 (실시간 모니터링) | FE | ⚪ |
| P3-C5 | Lit 컴포넌트 재사용성 80% 이상 | FE | ⚪ |
| P3-C6 | PWA 지원 (오프라인 동작, 모바일 설치) | FE | ⚪ |

---

## Phase 4: 통합 및 최적화 (Week 31-36)

### 4.1 통합 테스트 (Week 31-32)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 4.1.1 | QA | E2E 시나리오 작성 | E2E 테스트 시나리오 작성 | Week 31-Day 1 | ⚪ | 0% | 6개 워크플로우 |
| 4.1.2 | QA | 주문 처리 E2E | 주문 수신→분배→처리→실적 E2E 테스트 | Week 31-Day 2 | ⚪ | 0% | Selenium/Playwright |
| 4.1.3 | QA | 입고 PDA E2E | 입고 PDA 워크플로우 E2E 테스트 | Week 31-Day 3 | ⚪ | 0% | 모바일 에뮬레이터 |
| 4.1.4 | QA | PTL E2E | Put-To-Light 워크플로우 E2E 테스트 | Week 31-Day 4 | ⚪ | 0% | 표시기 시뮬레이터 |
| 4.1.5 | QA | P-TL E2E | Pick-To-Light 워크플로우 E2E 테스트 | Week 31-Day 5 | ⚪ | 0% | 표시기 시뮬레이터 |
| 4.1.6 | QA | P-DAS E2E | P-DAS 워크플로우 E2E 테스트 | Week 32-Day 1 | ⚪ | 0% | PDA 에뮬레이터 |
| 4.1.7 | QA | SMS E2E | SMS 워크플로우 E2E 테스트 | Week 32-Day 2 | ⚪ | 0% | Sorter 시뮬레이터 |
| 4.1.8 | QA | 성능 테스트 준비 | 성능 테스트 환경 및 스크립트 준비 | Week 32-Day 3 | ⚪ | 0% | JMeter/k6 |
| 4.1.9 | QA | 부하 테스트 | 대량 주문 처리 성능 테스트 (10,000건/시) | Week 32-Day 4 | ⚪ | 0% | 병목 지점 식별 |
| 4.1.10 | QA | 동시 접속 테스트 | 동시 접속 부하 테스트 (100명) | Week 32-Day 5 | ⚪ | 0% | API 응답 시간 |
| 4.1.11 | QA | 보안 스캔 | OWASP Top 10 취약점 스캔 | Week 32-Day 5 | ⚪ | 0% | OWASP ZAP |

### 4.2 성능 최적화 (Week 33-34)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 4.2.1 | BE | DB 인덱스 | DB 인덱스 최적화 (슬로우 쿼리 개선) | Week 33-Day 1 | ⚪ | 0% | EXPLAIN ANALYZE |
| 4.2.2 | BE | N+1 문제 해결 | JPA N+1 문제 해결 (fetch join) | Week 33-Day 2 | ⚪ | 0% | 쿼리 최적화 |
| 4.2.3 | BE | Redis 캐싱 | Redis 캐싱 전략 구현 (주문, 설비 상태) | Week 33-Day 3 | ⚪ | 0% | TTL 설정 |
| 4.2.4 | BE | Connection Pool | DB Connection Pool 튜닝 | Week 33-Day 4 | ⚪ | 0% | HikariCP |
| 4.2.5 | BE | JVM 튜닝 | JVM 메모리 튜닝 (Heap, GC) | Week 33-Day 5 | ⚪ | 0% | -Xms, -Xmx |
| 4.2.6 | FE | Lazy Loading | 페이지·컴포넌트 Lazy Loading | Week 34-Day 1 | ⚪ | 0% | Dynamic Import |
| 4.2.7 | FE | 번들 최적화 | 번들 크기 최적화 (Code Splitting) | Week 34-Day 2 | ⚪ | 0% | Vite Rollup |
| 4.2.8 | FE | 이미지 최적화 | 이미지 최적화 (WebP, Lazy Load) | Week 34-Day 3 | ⚪ | 0% | Intersection Observer |
| 4.2.9 | FE | Service Worker | Service Worker 캐싱 전략 | Week 34-Day 4 | ⚪ | 0% | Workbox |
| 4.2.10 | FE | Lighthouse 최적화 | Lighthouse 점수 90점 이상 달성 | Week 34-Day 5 | ⚪ | 0% | 성능, 접근성, SEO |
| 4.2.11 | BE | RabbitMQ 튜닝 | RabbitMQ Prefetch Count 튜닝 | Week 34-Day 3 | ⚪ | 0% | 메시지 처리량 |
| 4.2.12 | BE | DLQ 설정 | Dead Letter Queue 설정 | Week 34-Day 4 | ⚪ | 0% | 에러 메시지 재처리 |

### 4.3 보안 강화 (Week 35)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 4.3.1 | DO | HTTPS 설정 | SSL/TLS 인증서 발급 및 적용 | Week 35-Day 1 | ⚪ | 0% | Let's Encrypt |
| 4.3.2 | BE | Rate Limiting | API Rate Limiting 구현 | Week 35-Day 2 | ⚪ | 0% | Spring Security |
| 4.3.3 | BE | CORS 설정 | CORS 설정 강화 | Week 35-Day 2 | ⚪ | 0% | 허용 도메인 제한 |
| 4.3.4 | FE | 보안 헤더 | 보안 헤더 설정 (CSP, X-Frame-Options) | Week 35-Day 3 | ⚪ | 0% | Helmet.js 유사 |
| 4.3.5 | BE | 암호화 | 민감 정보 암호화 (DB 저장) | Week 35-Day 4 | ⚪ | 0% | AES-256 |
| 4.3.6 | BE | 감사 로그 | 감사 로그 (Audit Log) 구현 | Week 35-Day 5 | ⚪ | 0% | 사용자 행위 추적 |
| 4.3.7 | QA | 보안 재검증 | 보안 취약점 재검증 | Week 35-Day 5 | ⚪ | 0% | OWASP ZAP |

### 4.4 운영 환경 배포 준비 (Week 36)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 4.4.1 | DO | Docker 이미지 최적화 | 백엔드 Docker 이미지 최적화 | Week 36-Day 1 | ⚪ | 0% | 멀티스테이지 빌드 |
| 4.4.2 | DO | Nginx 설정 | Nginx Docker 이미지 및 설정 | Week 36-Day 1 | ⚪ | 0% | nginx.conf |
| 4.4.3 | DO | Docker Compose | Docker Compose 운영 환경 설정 | Week 36-Day 2 | ⚪ | 0% | docker-compose.prod.yml |
| 4.4.4 | DO | CI/CD 완성 | GitHub Actions CI/CD 파이프라인 완성 | Week 36-Day 2 | ⚪ | 0% | 빌드·테스트·배포 |
| 4.4.5 | DO | 배포 스크립트 | 운영 서버 배포 스크립트 작성 | Week 36-Day 3 | ⚪ | 0% | deploy.sh |
| 4.4.6 | DO | Blue-Green 배포 | Blue-Green 배포 전략 구현 | Week 36-Day 3 | ⚪ | 0% | 무중단 배포 |
| 4.4.7 | DO | Actuator 설정 | Spring Boot Actuator 설정 | Week 36-Day 4 | ⚪ | 0% | health, metrics |
| 4.4.8 | DO | Prometheus | Prometheus + Grafana 대시보드 | Week 36-Day 4 | ⚪ | 0% | 메트릭 수집 |
| 4.4.9 | DO | ELK Stack | ELK Stack 구축 (로깅) | Week 36-Day 5 | ⚪ | 0% | Logstash, Kibana |
| 4.4.10 | DO | 알림 설정 | 알림 설정 (Slack, Email) | Week 36-Day 5 | ⚪ | 0% | 장애 알림 |
| 4.4.11 | PM | API 문서 | Swagger/OpenAPI 문서 완성 | Week 36-Day 3 | ⚪ | 0% | Springdoc |
| 4.4.12 | PM | 운영 가이드 | 운영 가이드 작성 | Week 36-Day 4 | ⚪ | 0% | docs/operations/ |
| 4.4.13 | PM | 장애 대응 매뉴얼 | 장애 대응 매뉴얼 작성 | Week 36-Day 5 | ⚪ | 0% | docs/incident/ |
| 4.4.14 | PM | 백업·복구 절차 | 백업·복구 절차 문서화 | Week 36-Day 5 | ⚪ | 0% | docs/backup/ |

### Phase 4 완료 기준 체크리스트

| ID | 완료 기준 | 담당 | 완료 여부 |
|----|----------|------|----------|
| P4-C1 | E2E 테스트 통과 (모든 워크플로우) | QA | ⚪ |
| P4-C2 | 성능 목표 달성 (10,000건/시 주문 처리) | BE, QA | ⚪ |
| P4-C3 | 보안 취약점 0건 | BE, FE, QA | ⚪ |
| P4-C4 | 운영 환경 배포 완료 | DO | ⚪ |
| P4-C5 | 모니터링 대시보드 구축 완료 | DO | ⚪ |

---

## Phase 5+: 추가 확장 모듈 (Week 37+)

### 5.1 AGV 연동 모듈 (TBD)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 5.1.1 | BE | AGV 등록 | AGV 설비 등록 및 관리 API | TBD | ⚪ | 0% | agv_devices 테이블 |
| 5.1.2 | BE | 경로 최적화 | 이동 경로 최적화 알고리즘 | TBD | ⚪ | 0% | A* 또는 Dijkstra |
| 5.1.3 | BE | 작업 할당 | AGV 작업 할당 로직 | TBD | ⚪ | 0% | 최근접 AGV 선택 |
| 5.1.4 | BE | 충돌 방지 | 충돌 방지 알고리즘 | TBD | ⚪ | 0% | 예약 시스템 |
| 5.1.5 | FE | AGV 모니터링 | AGV 실시간 모니터링 화면 | TBD | ⚪ | 0% | 지도 위 AGV 위치 |

### 5.2 AS/RS 연동 모듈 (TBD)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 5.2.1 | BE | AS/RS 연동 | AS/RS 설비 RabbitMQ 연동 | TBD | ⚪ | 0% | wcs.command.asrs.* |
| 5.2.2 | BE | 입출고 명령 | 입출고 명령 처리 API | TBD | ⚪ | 0% | POST /api/asrs/in-out |
| 5.2.3 | BE | 재고 위치 | 재고 위치 관리 시스템 | TBD | ⚪ | 0% | inventory_locations |
| 5.2.4 | FE | 3D 재고 | 3D 재고 모니터링 화면 | TBD | ⚪ | 0% | Three.js |

### 5.3 AI 기반 작업 최적화 (TBD)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 5.3.1 | BE | 주문 패턴 분석 | 주문 패턴 분석 (시계열 분석) | TBD | ⚪ | 0% | Python ML 모델 |
| 5.3.2 | BE | 설비 Capa 예측 | 설비 용량 예측 모델 | TBD | ⚪ | 0% | LSTM/Prophet |
| 5.3.3 | BE | 작업 분배 최적화 | ML 기반 작업 분배 최적화 | TBD | ⚪ | 0% | 강화 학습 |
| 5.3.4 | BE | 이상 감지 | 설비 이상 감지 시스템 | TBD | ⚪ | 0% | Anomaly Detection |

### 5.4 모바일 앱 (React Native) (TBD)

| ID | 역할 | 작업 구분 | 작업 상세 | 완료 일정 | 완료 여부 | 진행률 | 비고 |
|----|------|----------|----------|----------|----------|--------|------|
| 5.4.1 | FE | RN 프로젝트 초기화 | React Native 프로젝트 초기화 | TBD | ⚪ | 0% | iOS/Android |
| 5.4.2 | FE | PDA 네이티브 구현 | PDA 기능 네이티브 구현 | TBD | ⚪ | 0% | 카메라, GPS |
| 5.4.3 | FE | 푸시 알림 | 네이티브 푸시 알림 | TBD | ⚪ | 0% | FCM |
| 5.4.4 | FE | 오프라인 모드 | 네이티브 오프라인 모드 | TBD | ⚪ | 0% | SQLite |

---

## 진행률 요약 (Overall Progress)

| Phase | 기간 | 전체 태스크 | 완료 | 진행 중 | 미시작 | 진행률 |
|-------|------|----------|------|--------|--------|--------|
| Phase 1: 코어 시스템 | Week 1-8 | 67 | 0 | 0 | 67 | 0% |
| Phase 2: 설비 모듈 | Week 9-20 | 65 | 0 | 0 | 65 | 0% |
| Phase 3: 클라이언트 UI | Week 21-30 | 60 | 0 | 0 | 60 | 0% |
| Phase 4: 통합·최적화 | Week 31-36 | 55 | 0 | 0 | 55 | 0% |
| **전체** | **Week 1-36** | **247** | **0** | **0** | **247** | **0%** |

---

## 리스크 및 이슈 관리

### 현재 리스크

| ID | 리스크 | 영향도 | 확률 | 대응 방안 | 담당 | 상태 |
|----|--------|--------|------|----------|------|------|
| R-001 | RabbitMQ 성능 병목 | 높음 | 중간 | 메시지 처리량 모니터링, Queue 분산 | BE, DO | ⚪ |
| R-002 | 대량 주문 처리 성능 부족 | 높음 | 중간 | DB 인덱스 최적화, Redis 캐싱 | BE | ⚪ |
| R-003 | 설비 통신 장애 | 높음 | 높음 | DLQ, Retry 로직, 알림 | BE, DO | ⚪ |
| R-004 | 프론트엔드 번들 크기 비대화 | 중간 | 높음 | Code Splitting, Lazy Loading | FE | ⚪ |
| R-005 | 요구사항 변경 | 높음 | 높음 | Agile 스프린트, 주간 리뷰 | PM | ⚪ |
| R-006 | 설비 모듈 개발 지연 | 높음 | 중간 | 모듈 우선순위 조정, 병렬 개발 | BE, PM | ⚪ |

### 현재 이슈

| ID | 이슈 | 우선순위 | 담당 | 상태 | 해결 방안 |
|----|------|----------|------|------|----------|
| - | 이슈 없음 | - | - | - | - |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 2026-03-01 | 1.0.0 | WBS 최초 작성 | Claude Sonnet 4.5 |

---

## 참고 문서

- [operato-wcs-development-plan.md](operato-wcs-development-plan.md) — 프로젝트 전체 계획서
- [CLAUDE.md](/Users/shortstop/Git/operato-wcs-ai/CLAUDE.md) — 프로젝트 컨벤션 및 규칙
- [backend-refactoring-plan.md](/Users/shortstop/Git/operato-wcs-ai/docs/refactoring/backend-refactoring-plan.md) — 백엔드 리팩토링 계획
