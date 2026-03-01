# Operato WCS 제품 개발 프로젝트 계획

## 프로젝트 개요

### 제품 비전

**Operato WCS (Warehouse Control System)** 는 풀필먼트 센터의 입고·보관·출고·배송 프로세스 전반을 제어·모니터링하여 최대의 운영 퍼포먼스를 지원하는 현장 제어 시스템입니다.

**최종 목표**: 끊임없이 변화하는 물류 환경에 유연하고 신속하게 대응 가능한 **다목적 물류 시스템 구축**

### 핵심 가치 제안

| 가치 | 설명 |
|------|------|
| 설비 최적화 운영 | 자동화 설비의 모듈화(플러그인), 설비 Capa 기반 최적 작업 분배, 분류 설비 가동율 극대화 |
| 통합 모니터링 | 센터 전체 설비·재고 실시간 통합 모니터링 (3D 재고 모니터링 포함) |
| 유연하고 지속적인 관리 | 프로세스 변화에 신속 대응, 작업 이력 데이터 기반 설비 Capa 지속 업데이트 |

### 제품 특장점

- **Web 기반 프레임워크**: 운영자·작업자·현황판 UI 통합 제공, Responsive UI 및 PWA로 모바일 환경 지원
- **물류 특화 프레임워크**: 물류 작업 처리 컨테이너 기반, 자동화 설비를 플러그인 형태로 추가
- **설비 통합 운영 및 최적화**: 주문·작업 형태에 따라 최적 설비에 분배, 설비 가동율 극대화로 확실한 ROI 보장
- **높은 유연성**: 설비군별·작업 형태별 설정 프로파일 제공, 동적 커스텀 서비스 기반 프로세스 제어
- **통합 모니터링 제공**: 설비 상황 및 재고 현황을 웹 기반 모니터링 보드로 실시간 확인
- **물류환경 선진화**: 모바일, 로봇, AGV, IoT, 클라우드 기술을 활용한 스마트 물류환경 지원

### 기술 스택

| 구분 | 기술 | 버전 | 비고 |
|------|------|------|------|
| 백엔드 프레임워크 | Spring Boot | 3.4 (Stable) | Java 17+ 필요 |
| 빌드 도구 | Gradle | 최신 안정 버전 | Kotlin DSL 권장 |
| 프론트엔드 | Lit | 3.x | Web Components 표준 기반 |
| UI 빌드 도구 | Vite | 5.x | 빠른 개발 서버 및 빌드 |
| 메시지 브로커 | RabbitMQ | 3.13.x | WCS ↔ ECS 간 연동 |
| 데이터베이스 | PostgreSQL | 16.x | 운영 환경 기본 |
| 캐시 | Redis | 7.x | 세션, 실시간 데이터 캐싱 |
| 컨테이너 | Docker | 24.x | 배포 및 개발 환경 표준화 |
| 웹 서버 | Nginx | 1.25.x | 운영 환경 정적 파일 서빙 |

---

## 개발 전략

### 개발 원칙

1. **모듈화 우선**: 설비 모듈은 플러그인 구조로 독립적으로 개발·배포 가능
2. **API First**: REST API 우선 설계 후 UI 개발
3. **테스트 주도**: 핵심 비즈니스 로직은 테스트 커버리지 80% 이상
4. **점진적 배포**: 코어 시스템 → 설비 모듈 → UI 순차 개발
5. **성능 우선**: 실시간 모니터링 및 대량 주문 처리 성능 최적화

### 아키텍처 설계 원칙

- **계층 분리**: Presentation (API) - Application (Business Logic) - Domain (Entity) - Infrastructure (DB, Message)
- **이벤트 드리븐**: RabbitMQ 기반 비동기 메시지 처리
- **설비 플러그인**: SPI(Service Provider Interface) 패턴으로 설비 모듈 확장
- **상태 관리**: Redis 기반 분산 세션 및 실시간 상태 캐싱
- **마이크로프론트엔드**: Lit 컴포넌트 단위 독립 개발 및 조합

---

## Phase 1: 코어 시스템 구축 (8주)

### 목표

WCS의 핵심 기능(주문 수신, 분배, 실적 관리)과 공통 인프라를 구축하여 설비 모듈 개발 기반을 마련합니다.

### 주요 기능

#### 1.1 프로젝트 초기 설정 (1주)

**백엔드**
- [x] Spring Boot 3.4 프로젝트 초기화
- [x] Gradle 멀티 모듈 구조 설정
- [ ] 공통 라이브러리 모듈 분리 (`wcs-common`)
- [ ] 도메인 모듈 분리 (`wcs-domain`)
- [ ] API 모듈 분리 (`wcs-api`)

**프론트엔드**
- [ ] Lit + Vite + TypeScript 프로젝트 초기화
- [ ] 디자인 시스템 정의 (CSS Custom Properties)
- [ ] 공통 컴포넌트 라이브러리 구조 설정
- [ ] 라우터 설정 (Vaadin Router 또는 직접 구현)

**인프라**
- [x] Docker Compose 환경 구성 (PostgreSQL, RabbitMQ, Redis)
- [ ] CI/CD 파이프라인 초기 구성 (GitHub Actions)
- [ ] 개발 환경 문서화

#### 1.2 인증·인가 시스템 (1주)

- [ ] JWT 기반 인증 구현
- [ ] Spring Security 설정
- [ ] 사용자·권한·역할 관리 API
- [ ] 로그인 UI (Manager, Kiosk, PDA)
- [ ] 세션 관리 (Redis 기반)

**API 엔드포인트**
```
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/refresh
GET    /api/auth/me
GET    /api/users
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
GET    /api/roles
```

#### 1.3 마스터 데이터 관리 (2주)

**상품 마스터**
- [ ] 상품 등록·수정·조회·삭제 API
- [ ] 상품 카테고리 관리
- [ ] 바코드·SKU 관리
- [ ] 상품 관리 UI (Manager)

**설비 마스터**
- [ ] 설비 등록·수정·조회·삭제 API
- [ ] 설비 유형 정의 (Indicator, Sorter, Cart, AGV 등)
- [ ] 설비 상태 관리 (가용, 비가용, 점검 중)
- [ ] 설비 관리 UI (Manager)

**작업자 마스터**
- [ ] 작업자 등록·수정·조회·삭제 API
- [ ] 작업자 스킬 관리
- [ ] 작업자 출퇴근 관리
- [ ] 작업자 관리 UI (Manager)

**API 엔드포인트**
```
# 상품
GET    /api/products
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

# 설비
GET    /api/equipments
POST   /api/equipments
PUT    /api/equipments/{id}
PATCH  /api/equipments/{id}/status

# 작업자
GET    /api/workers
POST   /api/workers
PUT    /api/workers/{id}
PATCH  /api/workers/{id}/attendance
```

#### 1.4 주문 수신 및 분배 시스템 (2주)

**주문 수신**
- [ ] WMS 연동 인터페이스 (REST API 또는 MQ)
- [ ] 주문 수신 및 검증
- [ ] 주문 파싱 및 DB 저장
- [ ] 주문 상태 관리 (수신, 분배 대기, 분배 완료, 처리 중, 완료, 취소)

**주문 분배**
- [ ] 주문 분석 엔진 (주문 특성 분석)
- [ ] 설비 Capa 기반 분배 알고리즘
- [ ] 주문-설비 매핑 테이블 관리
- [ ] 분배 이력 관리

**API 엔드포인트**
```
POST   /api/orders/receive       # WMS → WCS 주문 수신
GET    /api/orders
GET    /api/orders/{id}
POST   /api/orders/{id}/distribute
GET    /api/orders/{id}/assignments
```

#### 1.5 실적 집계 및 보고 시스템 (1주)

**실적 수집**
- [ ] 설비별 작업 실적 수집
- [ ] 실적 데이터 검증 및 집계
- [ ] 실적 이력 관리

**실적 보고**
- [ ] WMS 실적 전송 인터페이스
- [ ] 실적 보고서 생성
- [ ] 실적 대시보드 UI

**API 엔드포인트**
```
POST   /api/results              # ECS → WCS 실적 수신
GET    /api/results
POST   /api/results/report       # WCS → WMS 실적 전송
GET    /api/results/summary
```

#### 1.6 RabbitMQ 메시지 인프라 (1주)

**메시지 브로커 설정**
- [ ] RabbitMQ Exchange, Queue, Routing Key 정의
- [ ] 메시지 스키마 정의 (JSON)
- [ ] 메시지 발행·구독 공통 라이브러리
- [ ] 메시지 이력 관리 (로깅)

**메시지 유형**
```
wcs.equipment.command.*     # WCS → ECS 제어 명령
wcs.equipment.status.*      # ECS → WCS 상태 보고
wcs.equipment.result.*      # ECS → WCS 작업 실적
```

### Phase 1 완료 기준

- ✅ 인증·인가 시스템 동작 (로그인, JWT 검증)
- ✅ 마스터 데이터 CRUD API 완성 (상품, 설비, 작업자)
- ✅ 주문 수신 및 분배 프로세스 동작
- ✅ 실적 수집 및 보고 프로세스 동작
- ✅ RabbitMQ 메시지 발행·구독 정상 동작
- ✅ 테스트 커버리지 60% 이상 (핵심 비즈니스 로직)

---

## Phase 2: 설비 모듈 개발 (12주)

### 목표

플러그인 구조의 설비 모듈을 개발하여 다양한 물류 설비를 WCS에서 제어할 수 있도록 합니다.

### 2.1 설비 플러그인 SPI 설계 (1주)

**플러그인 인터페이스 정의**
- [ ] `EquipmentPlugin` 인터페이스 설계
- [ ] 설비 생명주기 관리 (초기화, 시작, 정지, 종료)
- [ ] 설비 상태 모니터링 인터페이스
- [ ] 작업 할당 인터페이스
- [ ] 실적 보고 인터페이스

```java
public interface EquipmentPlugin {
    String getEquipmentType();
    void initialize(EquipmentConfig config);
    void start();
    void stop();
    void shutdown();

    EquipmentStatus getStatus();
    void assignWork(WorkOrder order);
    void cancelWork(String orderId);

    List<WorkResult> getResults();
}
```

### 2.2 입고 PDA 모듈 (2주)

**기능**
- [ ] PDA 등록 및 관리
- [ ] 바코드 스캔 처리
- [ ] 입고 검수 프로세스
- [ ] 로케이션 할당
- [ ] 입고 실적 보고

**API 엔드포인트**
```
GET    /api/inbound/pda/orders           # 입고 대기 주문 조회
POST   /api/inbound/pda/scan              # 바코드 스캔
POST   /api/inbound/pda/putaway           # 입고 처리
GET    /api/inbound/pda/locations         # 로케이션 조회
```

**PDA UI**
- [ ] 로그인 화면
- [ ] 입고 주문 목록
- [ ] 바코드 스캔 화면
- [ ] 로케이션 할당 화면
- [ ] 입고 완료 확인

### 2.3 Put-To-Light 모듈 (2주)

**기능**
- [ ] 셀-주문 매핑 관리
- [ ] 상품 스캔 시 셀 표시기 점등 제어
- [ ] 작업자 확인 처리
- [ ] 작업 완료 감지
- [ ] 실적 보고

**RabbitMQ 메시지**
```
# WCS → Indicator
wcs.equipment.command.indicator.on      # 셀 표시기 점등
wcs.equipment.command.indicator.off     # 셀 표시기 소등

# Indicator → WCS
wcs.equipment.status.indicator.confirm  # 작업자 확인 버튼 누름
```

**API 엔드포인트**
```
GET    /api/ptl/cells                     # 셀 목록 조회
POST   /api/ptl/mapping                   # 셀-주문 매핑
POST   /api/ptl/scan                      # 상품 스캔
GET    /api/ptl/status                    # 작업 진행 상태
```

### 2.4 Pick-To-Light 모듈 (2주)

**기능**
- [ ] 셀-상품 배치 관리
- [ ] 박스(주문) 스캔 처리
- [ ] 피킹 셀 표시기 점등 제어
- [ ] 수량 확인 및 완료 처리
- [ ] 실적 보고

**API 엔드포인트**
```
GET    /api/ptl/cells                     # 셀 목록 조회
POST   /api/ptl/placement                 # 셀-상품 배치
POST   /api/ptl/scan/box                  # 박스 스캔
POST   /api/ptl/confirm                   # 피킹 확인
```

### 2.5 P-DAS 모듈 (2주)

**기능**
- [ ] PDA 기반 소량 주문 처리
- [ ] 피킹 경로 최적화
- [ ] 피킹 리스트 생성
- [ ] 피킹 진행 상황 추적
- [ ] 실적 보고

**API 엔드포인트**
```
GET    /api/pdas/orders                   # 피킹 대기 주문
POST   /api/pdas/assign                   # 작업자 할당
GET    /api/pdas/picklist/{orderId}       # 피킹 리스트 조회
POST   /api/pdas/pick                     # 피킹 처리
POST   /api/pdas/complete                 # 피킹 완료
```

### 2.6 SMS (Sorter Management System) 모듈 (3주)

**기능**
- [ ] Sorter 설비 연동 (RabbitMQ)
- [ ] 대량 분류 작업 처리
- [ ] 슈트(Chute) 관리 및 매핑
- [ ] 분류 진행 상황 모니터링
- [ ] 불량품(No-Read) 처리
- [ ] 실적 보고

**RabbitMQ 메시지**
```
# WCS → Sorter
wcs.equipment.command.sorter.induct     # 투입 명령
wcs.equipment.command.sorter.divert     # 분류 명령

# Sorter → WCS
wcs.equipment.status.sorter.inducted    # 투입 완료
wcs.equipment.status.sorter.diverted    # 분류 완료
wcs.equipment.status.sorter.noread      # No-Read 발생
```

**API 엔드포인트**
```
GET    /api/sms/sorters                   # Sorter 목록
GET    /api/sms/chutes                    # 슈트 목록
POST   /api/sms/mapping                   # 슈트-주문 매핑
GET    /api/sms/status                    # 분류 진행 상태
POST   /api/sms/noread                    # No-Read 처리
```

### Phase 2 완료 기준

- ✅ 5개 설비 모듈 모두 플러그인으로 동작 (입고PDA, PTL, P-TL, P-DAS, SMS)
- ✅ RabbitMQ 메시지 기반 설비 제어 정상 동작
- ✅ 각 모듈별 실적 보고 정상 동작
- ✅ 설비 플러그인 동적 로딩·언로딩 가능
- ✅ 테스트 커버리지 70% 이상

---

## Phase 3: 클라이언트 UI 개발 (10주)

### 목표

Manager, Kiosk, PDA, Tablet, Monitoring Board 등 다양한 클라이언트 UI를 Lit 기반으로 개발합니다.

### 3.1 디자인 시스템 구축 (2주)

**공통 컴포넌트**
- [ ] Button, Input, Checkbox, Radio, Select, Textarea
- [ ] Table, Pagination, DataGrid
- [ ] Modal, Toast, Alert, Confirm
- [ ] Tab, Accordion, Dropdown
- [ ] Badge, Tag, Chip
- [ ] Progress Bar, Spinner, Skeleton

**레이아웃 컴포넌트**
- [ ] Header, Sidebar, Footer
- [ ] Page Layout (1-column, 2-column, 3-column)
- [ ] Card, Panel, Section
- [ ] Responsive Grid

**테마 및 스타일**
- [ ] CSS Custom Properties 정의
- [ ] Light/Dark 테마 지원
- [ ] 모바일·태블릿·데스크톱 반응형 설계

### 3.2 Manager UI (3주)

**대시보드**
- [ ] 주문 현황 요약 (금일 입고/출고/완료)
- [ ] 설비 상태 요약 (가동/점검/오류)
- [ ] 작업자 현황 (출근/작업 중/휴게)
- [ ] 실시간 알림 (오류, 경고, 완료)

**주문 관리**
- [ ] 주문 목록 (필터링, 정렬, 검색)
- [ ] 주문 상세 조회
- [ ] 주문 분배 현황
- [ ] 주문 수동 분배
- [ ] 주문 취소

**설비 관리**
- [ ] 설비 목록
- [ ] 설비 상세 조회
- [ ] 설비 상태 변경 (가동/점검/정지)
- [ ] 설비 모니터링 (실시간)

**마스터 관리**
- [ ] 상품 관리 CRUD
- [ ] 작업자 관리 CRUD
- [ ] 설비 관리 CRUD
- [ ] 코드 관리

**실적 관리**
- [ ] 실적 조회 (일별, 월별, 연도별)
- [ ] 실적 대시보드 (차트)
- [ ] 실적 보고서 다운로드 (Excel, PDF)

### 3.3 Kiosk UI (1주)

- [ ] 작업자 로그인 (바코드 스캔)
- [ ] 작업자 출근·퇴근 처리
- [ ] 작업 지시 확인
- [ ] 휴게 시간 관리

### 3.4 PDA UI (2주)

**입고 PDA**
- [ ] 입고 주문 목록
- [ ] 바코드 스캔
- [ ] 로케이션 할당
- [ ] 입고 완료 처리

**P-DAS PDA**
- [ ] 피킹 주문 목록
- [ ] 피킹 리스트 조회
- [ ] 바코드 스캔
- [ ] 피킹 완료 처리

**공통**
- [ ] 로그인·로그아웃
- [ ] 작업 이력 조회
- [ ] 설정

### 3.5 Monitoring Board UI (2주)

**실시간 모니터링**
- [ ] 설비 상태 맵 (전체 설비 한눈에 보기)
- [ ] 주문 진행 현황 (파이 차트, 바 차트)
- [ ] 작업자 현황 (출근 인원, 작업 중 인원)
- [ ] 실시간 알림 (오류, 경고)
- [ ] 3D 재고 모니터링 (선택 사항)

**자동 갱신**
- [ ] WebSocket 기반 실시간 데이터 갱신
- [ ] 대형 화면 최적화 (TV, 모니터)

### Phase 3 완료 기준

- ✅ Manager UI 완성 (대시보드, 주문·설비·마스터·실적 관리)
- ✅ Kiosk UI 완성 (작업자 로그인, 출퇴근)
- ✅ PDA UI 완성 (입고, P-DAS)
- ✅ Monitoring Board UI 완성 (실시간 모니터링)
- ✅ Lit 컴포넌트 재사용성 80% 이상
- ✅ PWA 지원 (오프라인 동작, 모바일 설치)

---

## Phase 4: 통합 및 최적화 (6주)

### 목표

시스템 전체 통합 테스트, 성능 최적화, 보안 강화, 운영 환경 배포 준비를 완료합니다.

### 4.1 통합 테스트 (2주)

**E2E 테스트 시나리오**
- [ ] 주문 수신 → 분배 → 설비 처리 → 실적 보고 (전체 흐름)
- [ ] 입고 PDA 워크플로우
- [ ] Put-To-Light 워크플로우
- [ ] Pick-To-Light 워크플로우
- [ ] P-DAS 워크플로우
- [ ] SMS 워크플로우

**성능 테스트**
- [ ] 대량 주문 처리 성능 (10,000건/시)
- [ ] 동시 접속 부하 테스트 (100명 작업자)
- [ ] RabbitMQ 메시지 처리량 (1,000 msg/sec)
- [ ] DB 쿼리 성능 (응답 시간 100ms 이하)

**보안 테스트**
- [ ] OWASP Top 10 취약점 스캔
- [ ] JWT 토큰 보안 검증
- [ ] API 권한 검증
- [ ] SQL Injection, XSS 방어 확인

### 4.2 성능 최적화 (2주)

**백엔드 최적화**
- [ ] DB 인덱스 최적화
- [ ] 쿼리 N+1 문제 해결
- [ ] Redis 캐싱 전략 (주문, 설비 상태)
- [ ] Connection Pool 튜닝
- [ ] JVM 메모리 튜닝

**프론트엔드 최적화**
- [ ] Lazy Loading (페이지, 컴포넌트)
- [ ] 번들 크기 최적화 (Code Splitting)
- [ ] 이미지 최적화 (WebP, Lazy Load)
- [ ] Service Worker 캐싱 전략
- [ ] Lighthouse 점수 90점 이상

**메시지 브로커 최적화**
- [ ] RabbitMQ Prefetch Count 튜닝
- [ ] Dead Letter Queue 설정
- [ ] Message TTL 설정
- [ ] Queue 분산 전략

### 4.3 보안 강화 (1주)

- [ ] HTTPS 적용 (SSL/TLS 인증서)
- [ ] API Rate Limiting
- [ ] CORS 설정
- [ ] Helmet.js (보안 헤더)
- [ ] 민감 정보 암호화 (DB 저장)
- [ ] 감사 로그 (Audit Log)

### 4.4 운영 환경 배포 준비 (1주)

**Docker 이미지**
- [ ] 백엔드 Docker 이미지 최적화
- [ ] Nginx Docker 이미지 설정
- [ ] Docker Compose 운영 환경 설정

**배포 자동화**
- [ ] GitHub Actions CI/CD 파이프라인
- [ ] 운영 서버 배포 스크립트
- [ ] Blue-Green 배포 전략
- [ ] 롤백 절차

**모니터링 및 로깅**
- [ ] Spring Boot Actuator 설정
- [ ] Prometheus + Grafana 대시보드
- [ ] ELK Stack (Elasticsearch, Logstash, Kibana)
- [ ] 알림 설정 (Slack, Email)

**문서화**
- [ ] API 문서 (Swagger/OpenAPI)
- [ ] 운영 가이드
- [ ] 장애 대응 매뉴얼
- [ ] 백업·복구 절차

### Phase 4 완료 기준

- ✅ E2E 테스트 통과 (모든 워크플로우)
- ✅ 성능 목표 달성 (10,000건/시 주문 처리)
- ✅ 보안 취약점 0건
- ✅ 운영 환경 배포 완료
- ✅ 모니터링 대시보드 구축 완료

---

## 추가 확장 모듈 (Phase 5+)

### 5.1 AGV 연동 모듈

- [ ] AGV 설비 등록 및 관리
- [ ] 이동 경로 최적화
- [ ] 작업 할당 및 모니터링
- [ ] 충돌 방지 알고리즘

### 5.2 AS/RS 연동 모듈

- [ ] AS/RS 설비 연동
- [ ] 입출고 명령 처리
- [ ] 재고 위치 관리
- [ ] 3D 재고 모니터링

### 5.3 AI 기반 작업 최적화

- [ ] 주문 패턴 분석
- [ ] 설비 Capa 예측
- [ ] 작업 분배 최적화 (머신러닝)
- [ ] 이상 감지 (Anomaly Detection)

### 5.4 모바일 앱 (React Native)

- [ ] iOS/Android 네이티브 앱
- [ ] PDA 기능 네이티브 구현
- [ ] 푸시 알림
- [ ] 오프라인 모드

---

## 마일스톤 및 일정

| Phase | 기간 | 주요 마일스톤 | 산출물 |
|-------|------|--------------|--------|
| **Phase 1: 코어 시스템** | 8주 | 주문 수신·분배·실적 보고 완성 | API 문서, DB 스키마, 테스트 |
| **Phase 2: 설비 모듈** | 12주 | 5개 설비 모듈 완성 | 플러그인 SPI, 설비 모듈, 테스트 |
| **Phase 3: 클라이언트 UI** | 10주 | Manager/Kiosk/PDA/Board UI 완성 | Lit 컴포넌트, PWA, UI 테스트 |
| **Phase 4: 통합·최적화** | 6주 | 운영 환경 배포 완료 | 배포 스크립트, 모니터링, 문서 |
| **Phase 5+: 확장 모듈** | TBD | AGV, AS/RS, AI 최적화, 모바일 앱 | 추가 모듈 |

**전체 일정**: 약 36주 (9개월)

### 주요 일정

```
Week 1-8:    Phase 1 (코어 시스템)
Week 9-20:   Phase 2 (설비 모듈)
Week 21-30:  Phase 3 (클라이언트 UI)
Week 31-36:  Phase 4 (통합·최적화)
Week 37+:    Phase 5+ (확장 모듈)
```

---

## 리소스 계획

### 개발 팀 구성

| 역할 | 인원 | 주요 업무 |
|------|------|----------|
| 백엔드 개발자 | 3명 | Spring Boot, API, 설비 모듈, DB |
| 프론트엔드 개발자 | 2명 | Lit, UI 컴포넌트, PWA |
| 풀스택 개발자 | 1명 | 백엔드·프론트엔드 지원 |
| DevOps 엔지니어 | 1명 | Docker, CI/CD, 모니터링, 배포 |
| QA 엔지니어 | 1명 | 테스트 자동화, 성능 테스트, 보안 테스트 |
| 프로젝트 매니저 | 1명 | 일정 관리, 리스크 관리, 의사소통 |

**총 인원**: 9명

### 인프라 요구사항

**개발 환경**
- Docker Desktop (로컬 개발)
- PostgreSQL, RabbitMQ, Redis (Docker Compose)
- GitHub (코드 저장소)
- GitHub Actions (CI/CD)

**운영 환경**
- 클라우드 인스턴스 (AWS EC2, GCP Compute Engine 등) 또는 온프레미스 서버
- 백엔드: 2-4 vCPU, 8-16GB RAM, 100GB SSD (x2대 이상)
- DB: 4 vCPU, 16GB RAM, 500GB SSD (고가용성 구성)
- RabbitMQ: 2 vCPU, 4GB RAM, 50GB SSD
- Redis: 2 vCPU, 4GB RAM, 20GB SSD
- Nginx: 1 vCPU, 2GB RAM, 20GB SSD

---

## 리스크 관리

### 기술 리스크

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|----------|
| RabbitMQ 성능 병목 | 높음 | 중간 | 메시지 처리량 모니터링, Queue 분산, Prefetch 튜닝 |
| 대량 주문 처리 성능 부족 | 높음 | 중간 | DB 인덱스 최적화, Redis 캐싱, 배치 처리 |
| 설비 통신 장애 | 높음 | 높음 | Dead Letter Queue, Retry 로직, 알림 |
| 프론트엔드 번들 크기 비대화 | 중간 | 높음 | Code Splitting, Lazy Loading, Tree Shaking |

### 일정 리스크

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|----------|
| 요구사항 변경 | 높음 | 높음 | Agile 방식 적용, 스프린트 단위 개발 |
| 설비 모듈 개발 지연 | 높음 | 중간 | 모듈 우선순위 조정, 병렬 개발 |
| 인력 부족 | 중간 | 중간 | 외부 인력 투입, 일정 조정 |

### 운영 리스크

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|----------|
| 운영 환경 장애 | 높음 | 중간 | 고가용성 구성, 백업·복구 절차, 모니터링 |
| 보안 취약점 발견 | 높음 | 중간 | 정기 보안 스캔, 패치 관리, 침입 탐지 |
| 데이터 손실 | 높음 | 낮음 | 정기 백업, RAID 구성, 복제 |

---

## 성공 기준

### 기능 요구사항

- ✅ 주문 수신·분배·실적 보고 프로세스 정상 동작
- ✅ 5개 설비 모듈 플러그인 방식 동작
- ✅ Manager/Kiosk/PDA/Monitoring Board UI 완성
- ✅ RabbitMQ 기반 설비 제어 정상 동작

### 성능 요구사항

- ✅ 주문 처리량: 10,000건/시 이상
- ✅ API 응답 시간: 평균 100ms 이하
- ✅ 동시 접속: 100명 이상 처리
- ✅ 메시지 처리량: 1,000 msg/sec 이상

### 품질 요구사항

- ✅ 테스트 커버리지: 80% 이상
- ✅ 보안 취약점: 0건
- ✅ 코드 리뷰: 100% (모든 PR)
- ✅ API 문서화: 100%

### 운영 요구사항

- ✅ 가용성: 99.9% 이상
- ✅ 백업: 일 1회 자동 백업
- ✅ 모니터링: 실시간 대시보드 구축
- ✅ 장애 대응: 1시간 이내 복구

---

## 버전 정보

- **프로젝트**: Operato WCS
- **버전**: 1.0.0 (예정)
- **계획 수립일**: 2026-03-01
- **작성자**: Claude Sonnet 4.5
- **검토자**: (추후 추가)

---

## 참고 자료

- [CLAUDE.md](/Users/shortstop/Git/operato-wcs-ai/CLAUDE.md) — 프로젝트 전체 컨벤션 및 규칙
- [docs/refactoring/backend-refactoring-plan.md](/Users/shortstop/Git/operato-wcs-ai/docs/refactoring/backend-refactoring-plan.md) — 백엔드 리팩토링 계획
- [.claude/skills/README.md](/Users/shortstop/Git/operato-wcs-ai/.claude/skills/README.md) — Claude Code 스킬 사용 가이드

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|----------|--------|
| 2026-03-01 | 1.0.0 | 최초 작성 | Claude Sonnet 4.5 |
