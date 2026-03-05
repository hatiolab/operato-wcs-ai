# 코드 품질 분석 보고서

> 분석일: 2026-03-05 | 대상: operato-wcs-ai 전체 Java 소스

---

## 1. 코드 규모

| 항목 | 수치 |
|------|------|
| Java 파일 수 | 470개 |
| 총 코드 라인 수 | 약 81,000 LOC |
| REST Controller | 53개 |
| Service 클래스 | 212개 |
| Entity/Model | 152개 |
| Query Store | 25개 |
| Event Handler | 42개 |
| Configuration | 14개 |
| Utility | 24개 |

---

## 2. 패키지 구조

### 비즈니스 모듈 (operato.logis)

| 패키지 | 파일 수 | 설명 |
|--------|---------|------|
| operato.logis.das | 33 | DAS (Goods-to-Person) |
| operato.logis.dps | 28 | DPS (Digital Picking System) |
| operato.logis.dpc | 23 | DPC (Pick-to-Light Carton) |
| operato.logis.sms | 23 | SMS (Sorter Management System) |
| operato.logis.pdas | 23 | P-DAS (Pallet DAS) |
| operato.logis.wcs | 19 | WCS Core |
| operato.logis.sps | 18 | SPS (Sorter Placing System) |
| operato.logis.bms | 10 | BMS (Box Management) |
| operato.logis.ops | 7 | Operations |
| operato.logis.insp | 7 | Inspection |

### 프레임워크/공통 모듈

| 패키지 | 파일 수 | 설명 |
|--------|---------|------|
| xyz.anythings.base | 170 | 프레임워크 기반 클래스 |
| xyz.anythings.gw | 92 | 게이트웨이 (설비 통신) |
| xyz.anythings.comm | 8 | RabbitMQ 통신 |
| operato.gw.mqbase | 6 | MQ 기반 게이트웨이 |
| xyz.elidom | 2 | 프린트 서비스 |

### 모듈별 내부 구조 (일관성 양호)

각 모듈은 아래 레이어 구조를 따릅니다:

```
module/
├── config/    — Spring 설정
├── service/   — 비즈니스 로직 및 인터페이스
├── web/       — REST 컨트롤러
├── model/     — DTO
├── query/     — 쿼리 빌더/저장소
└── entity/    — 데이터 엔티티
```

---

## 3. 대형 파일 (Top 10)

| 순위 | 파일 | 라인 수 | 문제 |
|------|------|---------|------|
| 1 | `xyz.anythings.base.rest.DeviceProcessController` | 1,696 | God Class — 다중 책임 |
| 2 | `operato.logis.das.service.impl.DasAssortService` | 1,483 | 과도한 분류 로직 집중 |
| 3 | `operato.logis.dpc.service.impl.DpcPickingService` | 1,156 | 복잡한 피킹 워크플로우 |
| 4 | `xyz.anythings.base.entity.Order` | 875 | 엔티티 필드 과다 |
| 5 | `operato.logis.das.service.impl.DasPreprocessService` | 853 | 전처리 로직 집중 |
| 6 | `xyz.anythings.base.service.impl.AbstractPickingService` | 845 | 추상 클래스 비대화 |
| 7 | `operato.logis.dps.service.impl.DpsInstructionService` | 824 | 작업 지시 처리 |
| 8 | `operato.gw.mqbase.service.impl.MqbaseIndRequestService` | 824 | MQ 메시지 처리 |
| 9 | `xyz.anythings.base.service.util.BatchJobConfigUtil` | 815 | 설정 유틸리티 |
| 10 | `xyz.anythings.base.entity.JobInstance` | 810 | 엔티티 필드 과다 |

---

## 4. 코드 품질 이슈

### 4.1 God Class (1,000줄 이상)

**DeviceProcessController** (1,696줄)이 가장 심각합니다.
- 디바이스 프로세스, 배치 진행, 분류, 박싱, 피킹 등 다중 책임
- 테스트/유지보수/확장이 어려움
- 권장: DeviceUpdateController, BatchProgressController, ClassificationController 등으로 분리

**DasAssortService** (1,483줄)도 리팩토링 대상입니다.
- 분류 로직, 전처리, 배치 관리, 커스텀 서비스 처리가 혼재
- 권장: DasClassificationStrategy, DasValidationService 등으로 분리

### 4.2 장문 메서드 (50줄 이상)

`DasAssortService.classCellMapping()` 등 다수의 메서드가 50줄을 초과합니다.
- 6~7단계의 검증 로직이 하나의 메서드에 집중
- 비즈니스 로직과 검증 로직이 혼재
- 권장: 검증 로직을 별도 메서드 또는 Validator 클래스로 추출

### 4.3 의존성 주입 패턴

| 패턴 | 사용 횟수 | 평가 |
|------|-----------|------|
| @Autowired 필드 주입 | 268회 | 테스트 어려움 |
| 생성자 주입 | 0회 | 미사용 |

모든 DI가 필드 주입으로 되어 있어 다음 문제가 있습니다:
- 단위 테스트 시 Mock 주입이 어려움
- 의존성 요구사항이 숨겨짐
- 순환 의존성 감지 불가
- 권장: 생성자 주입으로 단계적 마이그레이션

### 4.4 예외 처리

| 패턴 | 사용 횟수 |
|------|-----------|
| 예외 발생 (throw) | 537회 |
| try-catch 블록 | 22회 |

- `ThrowUtil.newValidationErrorWithNoLog()` 패턴이 일관적으로 사용됨 (양호)
- try-catch가 매우 적어 예상치 못한 예외에 대한 처리가 부족
- 빈 catch 블록은 없음 (양호)

### 4.5 로깅

- SLF4J 로거 초기화: 14개 클래스에서만 확인
- 대부분의 서비스 메서드에 로깅이 부재
- 예외 throw에 의존하여 에러 추적
- RabbitMQ 송수신 로깅 미흡
- 권장: 서비스 레이어에 DEBUG/INFO 레벨 로깅 추가

### 4.6 기타 양호 항목

- `System.out.println` 사용: 0건
- 빈 catch 블록: 0건
- 하드코딩된 크리덴셜: 0건 (Jasypt 암호화 적용)
- SQL Injection 위험: 0건 (파라미터화된 쿼리 사용)

---

## 5. 아키텍처 평가

### 5.1 레이어드 아키텍처: 양호

- Controller -> Service -> QueryManager 계층 준수
- 모든 컨트롤러가 `DynamicControllerSupport` 또는 `AbstractRestService` 상속
- 서비스가 인터페이스를 구현하고 추상 베이스 클래스 활용

### 5.2 이벤트 기반 아키텍처: 양호

- 42개 이벤트 핸들러로 느슨한 결합 구현
- `@EventListener`, `@TransactionalEventListener` 적절히 사용
- ClassifyEvent, DeviceEvent, GatewayInitEvent, InputEvent 등

### 5.3 데이터 접근: DBIST (커스텀 ORM)

```java
Query condition = AnyOrmUtil.newConditionForExecution(domainId);
condition.addFilter("batchId", batchId);
List<Order> orders = queryManager.selectList(Order.class, condition);
```

- JPA가 아닌 DBIST 커스텀 프레임워크 사용
- 파라미터화된 쿼리로 SQL Injection 방지
- Named Parameter 방식의 SQL 실행

### 5.4 트랜잭션 관리: 양호

- `@Transactional` 72개 인스턴스
- `@TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)` 적절히 사용
- rollback 규칙 명시 없음 (기본값 사용)

### 5.5 REST API 일관성: 양호

- 일관된 URL 패턴: `/rest/{resource}`
- 표준 페이징 파라미터: page, limit, select, sort, query
- 일관된 응답 형식 (BaseResponse)
- CRUD가 REST 관례 준수

---

## 6. 의존성 이슈

### 취약 라이브러리 (즉시 교체 필요)

| 라이브러리 | 현재 버전 | 문제 | 권장 조치 |
|-----------|----------|------|----------|
| ~~FastJSON~~ | ~~1.2.47~~ | ~~다수 CVE (RCE 취약점)~~ | ✅ 제거 완료 (미사용 확인) |
| ~~iText~~ | ~~4.2.2~~ | ~~EOL, 보안 취약점~~ | ✅ 제거 완료 (OpenPDF 유지) |
| Commons Collections | 3.2.2 | 역직렬화 취약점 | 4.x 업그레이드 (otarepo-core 수정 필요) |

### 구버전 라이브러리 (업그레이드 권장)

| 라이브러리 | 현재 버전 | 문제 | 권장 버전 |
|-----------|----------|------|----------|
| Apache Velocity | 1.7 | 2010년 릴리스, 미유지보수 | Velocity 2.3+ |
| Commons DBCP | 1.4 | 2006년 릴리스, 미유지보수 | HikariCP 또는 DBCP2 |
| ~~Apache Batik~~ | ~~1.14~~ | ~~구버전~~ | ✅ 1.17로 업그레이드 완료 |
| ~~Hutool~~ | ~~5.7.20~~ | ~~구버전~~ | ✅ 제거 완료 (미사용 확인) |
| ~~Groovy JSR223~~ | ~~5.0.0-alpha-4~~ | ~~알파 릴리스~~ | ✅ 4.0.24로 교체 완료 |
| ~~Barbecue~~ | ~~1.5-beta1~~ | ~~베타 릴리스, 미완성~~ | ✅ 제거 완료 (미사용 확인) |

### Deprecated Spring 설정

| 현재 속성 | 대체 속성 |
|----------|----------|
| ~~`spring.http.multipart.max-file-size`~~ | ✅ `spring.servlet.multipart.max-file-size`로 마이그레이션 완료 |
| ~~`spring.http.multipart.max-request-size`~~ | ✅ `spring.servlet.multipart.max-request-size`로 마이그레이션 완료 |
| ~~`endpoints.health.enabled`~~ | ✅ `management.endpoint.health.enabled`로 마이그레이션 완료 |
| ~~`endpoints.shutdown.enabled`~~ | ✅ `management.endpoint.shutdown.enabled`로 마이그레이션 완료 |
| ~~`management.security.enabled`~~ | ✅ 제거 완료 (Spring Security 6 설정 클래스로 관리) |
| ~~`security.basic.enabled`~~ | ✅ 제거 완료 (Spring Security 6 설정 클래스로 관리) |

### 기타 설정 이슈

- `spring.main.allow-circular-references=true` — 순환 의존성 허용 (아키텍처 이슈)
- XML 설정에서 Spring 3.1 스키마 사용 (현재 Spring 6.x)
- SHA-256 비밀번호 인코더 사용 (bcrypt 권장)
- ~~Docker 이미지에서 Java 18 사용~~ → ✅ Java 17로 통일 완료

---

## 7. 테스트 현황

| 항목 | 상태 |
|------|------|
| 단위 테스트 디렉토리 (`src/test/`) | ✅ 생성 완료 |
| JUnit 테스트 파일 | 0개 (프레임워크 구성 완료) |
| 통합 테스트 | 0개 |
| 테스트 엔드포인트 (개발용) | 2개 |

**테스트 부재는 가장 심각한 품질 이슈입니다.**

---

## 8. 종합 평가

| 카테고리 | 점수 | 평가 |
|----------|------|------|
| 아키텍처 | 8/10 | 레이어드 구조 양호, 일부 God Class 존재 |
| 코드 품질 | 6/10 | God Class, 장문 메서드, 필드 주입 문제 |
| 보안 | 9/10 | SQL Injection 방지 양호, FastJSON·iText 제거 완료 |
| 테스트 | 2/10 | 프레임워크 구성 완료, 테스트 작성 필요 |
| 유지보수성 | 6/10 | 모듈화 양호, 대형 클래스 리팩토링 필요 |
| 문서화 | 7/10 | 메서드 수준 주석 양호, API 문서 미작성 |
| 의존성 관리 | 8/10 | 미사용 제거, BOM 관리 전환, 의존성 그룹 정리 완료 |

**종합 점수: 6.6 / 10** (개선 전 5.7)
