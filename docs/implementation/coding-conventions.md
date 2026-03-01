# Operato WCS — 코딩 컨벤션 분석 및 가이드

## 문서 정보

| 항목 | 내용 |
|------|------|
| 작성일 | 2026-03-01 |
| 버전 | 1.0 |
| 대상 | 백엔드 Java 소스 (1,202개 파일) |
| 목적 | 현재 코드베이스의 코딩 컨벤션 분석, 부족한 점 식별 및 개선 가이드 |

---

## 1. 프로젝트 구조 개요

### 1.1 패키지 구조

```
src/main/java/
├── xyz.elidom.*          (634 파일) — 프레임워크 코어 (ORM, 보안, 시스템, 유틸)
├── xyz.anythings.*       (347 파일) — 물류 도메인 공통 (base, gw, sys, sec)
├── operato.logis.*       (216 파일) — ECS 모듈별 구현 (das, dps, sps, sms 등)
├── operato.core.*        (  2 파일) — Operato 코어
├── org.*                 (  2 파일) — 벤더 패치/확장
└── net.*                 (  1 파일) — 라이브러리 확장
```

### 1.2 파일 유형별 분포

| 유형 | 개수 | 비고 |
|------|------|------|
| Entity 클래스 | ~200 | dbist ORM 기반 |
| Controller | ~111 | `AbstractRestService` 상속 |
| Service | ~165 | `@Component` 사용 |
| Interface (I*) | ~163 | 서비스 API 정의 |
| Model/DTO | ~193 | `*/model/*` 패키지 |
| Utility | ~63 | `*Util*.java` |
| Event | ~60 | Spring 이벤트 기반 |
| Config | ~59 | 모듈별 설정 |
| Constants | ~52 | 상수 정의 |
| Exception | ~30 | 계층형 예외 구조 |
| Event Handler | ~28 | `@EventListener` 기반 |

---

## 2. 현재 코딩 컨벤션 (As-Is)

### 2.1 네이밍 규칙

#### 패키지 네이밍

```
xyz.elidom.{모듈}.{계층}          — 프레임워크
xyz.anythings.{모듈}.{계층}       — 물류 공통
operato.logis.{ECS유형}.{계층}    — ECS 모듈
```

**계층 구분 패키지:**
- `entity` — 엔티티 클래스
- `rest` — REST 컨트롤러 (`controller` 대신 `rest` 사용)
- `service` — 서비스 구현
- `service.api` — 서비스 인터페이스
- `service.impl` — 서비스 구현체
- `service.model` — 서비스 모델/DTO
- `event` — 이벤트 클래스
- `event.handler` — 이벤트 핸들러
- `config` — 설정 클래스
- `util` — 유틸리티

#### 클래스 네이밍

| 유형 | 패턴 | 예시 |
|------|------|------|
| Entity | `{도메인명}` (접미사 없음) | `Gateway`, `JobBatch`, `Indicator` |
| Controller | `{Entity}Controller` | `GatewayController`, `JobBatchController` |
| Service Interface | `I{기능}Service` | `IClassificationService`, `IBatchService` |
| Service 구현 | `{기능}Service` | `IndConfigProfileService`, `StockService` |
| Dispatcher | `{도메인}Dispatcher` | `LogisServiceDispatcher`, `IndicatorDispatcher` |
| Constants | `{모듈}Constants` | `GwConstants`, `LogisConstants` |
| Event | `{도메인}{동작}Event` | `GatewayBootEvent`, `IndicatorInitEvent` |
| Event Handler | `{도메인}EventHandler` | `MwErrorEventHandler` |
| Config | `ModuleProperties` | 모듈별 설정 프로퍼티 |

#### 필드/변수 네이밍

```java
// 필드: camelCase + 한국식 약어 사용
private String gwCd;        // 게이트웨이 코드 (gw = gateway, cd = code)
private String stageCd;     // 스테이지 코드
private String equipNm;     // 설비 명 (nm = name)
private String gwIp;        // 게이트웨이 IP

// DB 컬럼: snake_case
@Column(name = "gw_cd", nullable = false, length = 30)
```

**약어 규칙 (주요 패턴):**

| 약어 | 의미 | 예시 |
|------|------|------|
| `cd` | Code | `gwCd`, `stageCd`, `equipCd` |
| `nm` | Name | `gwNm`, `equipNm` |
| `qty` | Quantity | `batchOrderQty`, `resultBoxQty` |
| `no` | Number | `channelNo`, `panNo` |
| `ind` | Indicator | `indCd`, `indConfigSet` |

#### 상수 네이밍

```java
// UPPER_SNAKE_CASE, 각 상수마다 Javadoc 주석
public static final String IND_BIZ_FLAG_OK = "ok";
public static final String IND_BIZ_FLAG_MODIFY = "modify";
public static final String STATUS_RECEIVE = "RECEIVING";
public static final String STATUS_WAIT = "WAIT";
```

#### 메서드 네이밍

```java
// Controller CRUD — 고정 패턴
public Page<?> index(...)       // 목록 조회 (GET)
public Entity findOne(...)      // 단건 조회 (GET /{id})
public Boolean isExist(...)     // 존재 여부 (GET /{id}/exist)
public Entity create(...)       // 생성 (POST)
public Entity update(...)       // 수정 (PUT /{id})
public void delete(...)         // 삭제 (DELETE /{id})
public Boolean multipleUpdate(...)  // 일괄 처리 (POST /update_multiple)

// Service — 비즈니스 메서드
public Object classify(...)     // 분류 처리
public Object input(...)        // 투입 처리
public String checkInput(...)   // 유효성 체크
```

---

### 2.2 ORM 패턴 (dbist)

JPA/Hibernate가 아닌 **자체 ORM 프레임워크 (dbist)** 를 사용합니다.

#### Entity 정의

```java
@Table(name = "gateways", idStrategy = GenerationRule.UUID,
       uniqueFields = "domainId,gwCd", indexes = {
    @Index(name = "ix_gateways_0", columnList = "domain_id,gw_cd", unique = true),
    @Index(name = "ix_gateways_1", columnList = "domain_id,stage_cd")
})
public class Gateway extends ElidomStampHook {
    private static final long serialVersionUID = 511723720713339618L;

    @PrimaryKey
    @Column(name = "id", nullable = false, length = 40)
    private String id;

    @Column(name = "gw_cd", nullable = false, length = 30)
    private String gwCd;

    @Ignore  // DB 매핑 제외
    private String batchId;

    // 수동 getter/setter...
}
```

**핵심 규칙:**
- `@Table` — 테이블명 (복수형 snake_case: `gateways`, `job_batches`)
- `@PrimaryKey` + `GenerationRule.UUID` — UUID 기반 ID 생성
- `@Column` — 필드-컬럼 매핑 (snake_case 컬럼 ↔ camelCase 필드)
- `@Ignore` — DB 매핑 제외 필드
- `@Index` — 인덱스 정의 (네이밍: `ix_{테이블명}_{순번}`)
- `ElidomStampHook` 상속 — 생성일/수정일 자동 관리
- `serialVersionUID` 필수 선언

#### 쿼리 실행

```java
// QueryManager를 통한 SQL 직접 실행
String sql = "select * from gateways where id in (...)";
Map<String, Object> params = ValueUtil.newMap("domainId,equipType", domainId, equipType);
this.queryManager.selectListBySql(sql, params, Gateway.class, 0, 0);

// Query 객체 사용
Query query = new Query();
query.addFilter("domainId", Domain.currentDomainId());
this.queryManager.selectList(Gateway.class, query);
```

---

### 2.3 Controller 패턴

모든 컨트롤러는 `AbstractRestService`를 상속하며 동일한 템플릿을 따릅니다.

```java
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/gateways")
@ServiceDesc(description = "Gateway Service API")
public class GatewayController extends AbstractRestService {

    @Override
    protected Class<?> entityClass() {
        return Gateway.class;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Search (Pagination) By Search Conditions")
    public Page<?> index(...) {
        return this.search(this.entityClass(), page, limit, select, sort, query);
    }
    // ... 표준 CRUD 메서드
}
```

**규칙:**
- URL 패턴: `/rest/{엔티티 복수형}` (예: `/rest/gateways`, `/rest/job_batches`)
- `@RequestMapping` + `RequestMethod` 사용 (Spring MVC 레거시 스타일)
- `@ServiceDesc`, `@ApiDesc` 커스텀 어노테이션으로 API 문서화
- 클래스 레벨 `@Transactional` 적용
- 클래스 레벨 `@ResponseStatus(HttpStatus.OK)` 적용

---

### 2.4 Service 패턴

#### DI (의존성 주입)

```java
@Component  // @Service 대신 @Component 일관 사용
public class LogisServiceDispatcher implements BeanFactoryAware {

    @Autowired  // 필드 주입 일관 사용
    private ReceiveBatchService receiveBatchService;

    @Autowired
    private JobConfigProfileService configSetService;
}
```

#### Dispatcher 패턴

작업 유형(jobType)에 따라 동적으로 서비스 빈을 찾아 반환하는 패턴입니다.

```java
public IBatchService getBatchService(String jobType) {
    jobType = this.mapJobType(jobType);              // 소문자 변환
    String batchSvcType = jobType + "BatchService";  // 빈 이름 조합
    return (IBatchService) this.beanFactory.getBean(batchSvcType);
}
```

---

### 2.5 예외 처리 패턴

계층형 예외 구조를 사용합니다.

```
ElidomException (RuntimeException)
├── ElidomClientException (4xx)
│   ├── ElidomBadRequestException
│   ├── ElidomInputException
│   ├── ElidomInvalidParamsException
│   ├── ElidomRecordNotFoundException
│   ├── ElidomServiceNotFoundException
│   └── ElidomUnauthorizedException
└── ElidomServerException (5xx)
    ├── ElidomAlreadyExistException
    ├── ElidomDatabaseException
    ├── ElidomInvalidStatusException
    ├── ElidomLicenseException
    ├── ElidomOutputException
    ├── ElidomRuntimeException
    ├── ElidomScriptRuntimeException
    ├── ElidomServiceException
    └── ElidomValidationException
```

별도로 dbist ORM의 예외:
```
DbistException (Exception)
├── DataNotFoundException
└── DbistRuntimeException (RuntimeException)
```

---

### 2.6 이벤트 기반 아키텍처

Spring Event 기반의 느슨한 결합 구조를 사용합니다.

```java
// 이벤트 클래스
public class GatewayBootEvent extends AbstractGatewayEvent { ... }

// 이벤트 핸들러
@Component
public class MwErrorEventHandler {
    @EventListener(classes = MwErrorEvent.class)
    public void handleMwError(MwErrorEvent event) { ... }
}
```

---

### 2.7 Javadoc/주석 패턴

```java
/**
 * 작업 유형에 따른 서비스를 찾아주는 컴포넌트
 *
 * @author shortstop
 */
@Component
public class LogisServiceDispatcher { ... }
```

- 클래스: 한글 설명 + `@author`
- 인터페이스 메서드: 번호 체계 Javadoc (`1-1.`, `2-1.` 등)
- 필드: `/** 한글 설명 */` 형태
- 비즈니스 로직: 단계별 번호 주석 (`// 1. xxx`, `// 2. xxx`)

---

### 2.8 설정 파일 패턴

- `.properties` 형식 사용 (YAML 미사용)
- 섹션 구분: `########` 주석 블록
- 환경별 분리: `application-{profile}.properties`
- 모듈별 프로퍼티: `properties/operato-logis-{module}.properties`
- JSON 시드 데이터: `seeds/{패키지경로}/{Entity}.json`

---

### 2.9 빌드 및 의존성 패턴

- Spring Boot 3.2.4 + Java 17
- Gradle 빌드 (Groovy DSL)
- 커스텀 Maven 저장소 (`repo.hatiolab.com`)
- JPA 미사용, dbist ORM
- Lombok 미사용 (수동 getter/setter)

---

## 3. 부족한 점 및 개선사항

### 3.1 심각도: 높음 (즉시 개선 권장)

#### 3.1.1 테스트 코드 부재

**현재:** `src/test/` 디렉토리 자체가 존재하지 않습니다.

**문제점:**
- 리팩토링 시 회귀 버그를 감지할 수 없음
- 비즈니스 로직의 정확성을 보장할 수 없음
- CI/CD 파이프라인에서 품질 게이트가 없음

**권장:**
```
src/test/java/
├── xyz/anythings/base/service/   # 핵심 서비스 단위 테스트
├── xyz/anythings/gw/rest/        # 컨트롤러 통합 테스트
└── operato/logis/                # ECS 모듈 테스트
```

---

#### 3.1.2 필드 주입 의존

**현재:** 466개 `@Autowired` 필드 주입 (206개 파일)

```java
// 현재 (필드 주입)
@Component
public class SomeService {
    @Autowired
    private AnotherService anotherService;
}
```

**문제점:**
- 테스트 시 Mock 주입이 어려움 (Reflection 필요)
- 순환 의존성이 컴파일 타임에 감지되지 않음
- 불변 객체 패턴 불가 (`final` 필드 사용 불가)
- Spring 공식 가이드에서 비권장

**권장 (생성자 주입):**
```java
@Component
public class SomeService {
    private final AnotherService anotherService;

    public SomeService(AnotherService anotherService) {
        this.anotherService = anotherService;
    }
}
```

> 신규 코드부터 적용하고, 기존 코드는 리팩토링 시 점진적으로 변환합니다.

---

#### 3.1.3 설정 파일 내 민감 정보 하드코딩

**현재:** `application.properties`에 다음 정보가 평문으로 노출되어 있습니다.

```properties
spring.datasource.password=anythings
mail.smtp.password=1q2w3e4r~!
mq.broker.user.pw=admin
```

**권장:**
- 환경 변수 또는 `.env` 파일로 분리
- Jasypt 암호화 적용 (이미 의존성 존재: `jasypt-spring-boot-starter`)
- `.gitignore`에 `.env` 추가

---

### 3.2 심각도: 중간 (점진적 개선)

#### 3.2.1 `@Component` 단일 사용 — 역할 구분 부재

**현재:** 서비스, 이벤트 핸들러 등 모든 빈이 `@Component`로 등록됩니다.

**문제점:**
- 클래스의 역할(서비스, 리포지토리 등)이 어노테이션만으로 구분 불가
- IDE에서 계층별 필터링이 어려움
- Spring의 예외 변환 등 계층별 부가 기능 미활용

**권장 (신규 코드 적용):**

| 역할 | 현재 | 권장 |
|------|------|------|
| 서비스 | `@Component` | `@Service` |
| 데이터 접근 | `@Component` | `@Repository` (예외 변환 혜택) |
| 이벤트 핸들러 | `@Component` | `@Component` (유지) |
| 설정 | `@Component` | `@Configuration` |

---

#### 3.2.2 레거시 Spring MVC 어노테이션 사용

**현재:**
```java
@RequestMapping(value = "/{id}", method = RequestMethod.GET,
                produces = MediaType.APPLICATION_JSON_VALUE)
```

**권장 (신규 코드 적용):**
```java
@GetMapping("/{id}")
```

Spring 4.3+ 축약 어노테이션 (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`)으로 전환하면 가독성이 향상됩니다.

---

#### 3.2.3 Lombok 미사용 — 과도한 보일러플레이트

**현재:** 모든 Entity에 수동 getter/setter (Gateway.java: 코드의 60%가 getter/setter)

**권장:** Lombok 도입으로 코드량 대폭 감소

```java
// Before (150줄)
public class Gateway extends ElidomStampHook {
    private String id;
    private String gwCd;
    // ... 10개 필드
    // ... getter/setter 각 10개 = 60줄
}

// After (30줄)
@Getter @Setter
public class Gateway extends ElidomStampHook {
    private String id;
    private String gwCd;
    // ... 10개 필드
}
```

> dbist ORM과의 호환성 검증이 필요합니다. 프레임워크가 리플렉션 기반으로 getter/setter를 호출하는 경우 Lombok과 완벽 호환됩니다.

---

#### 3.2.4 현대 Java 기능 미활용

**현재:** Java 17 대상이나 Java 8 스타일로 작성

| 기능 | 사용률 | 권장 적용 |
|------|--------|----------|
| Stream API | ~10개 파일 (0.8%) | 컬렉션 처리 시 적극 활용 |
| Optional | 0개 파일 | null 반환 메서드에 적용 |
| `var` 키워드 | 1개 파일 | 지역 변수에 선택적 사용 |
| Record | 0개 파일 | 불변 DTO/Model에 적용 |
| Text Block | 0개 파일 | 인라인 SQL 등에 활용 |
| Switch Expression | 0개 파일 | 분기 로직에 활용 |

**예시 — Stream + Optional 활용:**
```java
// Before
List<String> result = new ArrayList<>();
for (Gateway gw : gateways) {
    if (gw.getStatus() != null && gw.getStatus().equals("active")) {
        result.add(gw.getGwCd());
    }
}

// After
List<String> result = gateways.stream()
    .filter(gw -> "active".equals(gw.getStatus()))
    .map(Gateway::getGwCd)
    .toList();
```

**예시 — Text Block으로 SQL 가독성 향상:**
```java
// Before
String sql = "select * from gateways where id in (select distinct(g.id) as gw_id from gateways g inner join indicators i on g.domain_id = i.domain_id and g.gw_cd = i.gw_cd inner join cells c on i.domain_id = c.domain_id and i.ind_cd = c.ind_cd where g.domain_id = :domainId and c.equip_type = :equipType and c.equip_cd = :equipCd)";

// After
String sql = """
    SELECT * FROM gateways
    WHERE id IN (
        SELECT DISTINCT g.id
        FROM gateways g
        INNER JOIN indicators i ON g.domain_id = i.domain_id AND g.gw_cd = i.gw_cd
        INNER JOIN cells c ON i.domain_id = c.domain_id AND i.ind_cd = c.ind_cd
        WHERE g.domain_id = :domainId
          AND c.equip_type = :equipType
          AND c.equip_cd = :equipCd
    )
    """;
```

---

#### 3.2.5 인라인 SQL과 QueryStore 혼용

**현재:** 컨트롤러에 직접 SQL이 작성되어 있습니다.

```java
// GatewayController.java — 컨트롤러에 SQL 하드코딩
@RequestMapping(value = "/search_by_equip/{equip_type}/{equip_cd}", ...)
public List<Gateway> searchByRegion(...) {
    String sql = "select * from gateways where id in (select distinct...";
    return this.queryManager.selectListBySql(sql, params, Gateway.class, 0, 0);
}
```

**문제점:**
- 컨트롤러가 데이터 접근 로직을 직접 포함
- SQL 변경 시 Java 코드 재컴파일 필요
- SQL 재사용 불가

**권장:**
- 쿼리는 QueryStore(외부 SQL 파일) 또는 서비스 계층으로 분리
- 컨트롤러는 서비스 호출만 담당

---

#### 3.2.6 Entity 내 비즈니스 로직/트랜잭션

**현재:** Entity 클래스에 `@Transactional` 메서드와 비즈니스 로직이 포함되어 있습니다.

```java
// JobBatch.java (Entity)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void someBusinessMethod() { ... }
```

**문제점:**
- Entity가 순수 데이터 객체 역할을 넘어서 서비스 로직을 포함
- `@Transactional`이 Entity에서 동작하려면 프록시 주의 필요
- 테스트가 어려움

**권장:** 비즈니스 로직은 서비스 계층으로 분리합니다.

---

### 3.3 심각도: 낮음 (향후 개선)

#### 3.3.1 import 정리 기준 미정립

**현재:** import 순서가 파일마다 다릅니다.

**권장 순서:**
```java
import java.*
import javax.*
import jakarta.*
                    // 빈 줄
import org.springframework.*
                    // 빈 줄
import xyz.elidom.*
import xyz.anythings.*
import operato.*
                    // 빈 줄
import 기타 서드파티
```

IDE의 `Organize Imports` 설정으로 자동화할 수 있습니다.

---

#### 3.3.2 Javadoc 커버리지 불균등

**현재:**
- 인터페이스 (`I*Service`): Javadoc 우수 (번호 체계, 상세 설명)
- 구현 클래스: 클래스 레벨만 있고 메서드 Javadoc 부족
- Entity: 필드 Javadoc 우수, 클래스 Javadoc 간략

**권장:**
- **public API (인터페이스, 컨트롤러)**: 필수 Javadoc
- **서비스 구현**: 인터페이스 상속으로 커버
- **Entity**: 현재 수준 유지

---

#### 3.3.3 일관되지 않은 약어 사용

| 약어 패턴 | 예시 | 비고 |
|-----------|------|------|
| `cd` (Code) | `gwCd`, `stageCd` | 일관적 |
| `nm` (Name) | `gwNm`, `equipNm` | 일관적 |
| `qty` (Quantity) | `orderQty` | 일관적 |
| `no` (Number) | `channelNo` | `num`과 혼용 주의 |
| 풀네임 | `status`, `version`, `remark` | 약어 미사용 |

> 전체적으로 약어 사용이 잘 통일되어 있으나, 새 필드 추가 시 기존 약어 규칙을 따르도록 합니다.

---

## 4. 신규 코드 작성 가이드

기존 코드와의 일관성을 유지하면서 점진적으로 개선합니다.

### 4.1 기존 규칙 유지 (필수)

| 항목 | 규칙 |
|------|------|
| 패키지 구조 | `{도메인}.{계층}` 체계 유지 |
| Entity | dbist 어노테이션, `ElidomStampHook` 상속, UUID ID |
| Controller | `AbstractRestService` 상속, `/rest/` prefix |
| 상수 | `UPPER_SNAKE_CASE`, 각 상수 Javadoc 필수 |
| 필드 약어 | `cd`, `nm`, `qty`, `no` 기존 규칙 준수 |
| 테이블 명 | 복수형 snake_case (`job_batches`, `gateways`) |
| 인덱스 명 | `ix_{테이블명}_{순번}` |
| 주석 언어 | 한글 (비즈니스 로직), 영문 (API 설명) |

### 4.2 신규 코드에서 적용할 개선 (권장)

| 항목 | 변경 내용 |
|------|----------|
| DI 방식 | 생성자 주입 사용 |
| 스테레오타입 | `@Service`, `@Repository` 역할별 구분 |
| HTTP 매핑 | `@GetMapping` 등 축약 어노테이션 |
| SQL 위치 | 컨트롤러가 아닌 서비스/QueryStore |
| Java 기능 | Stream, Optional, Text Block 적극 활용 |
| 불변 DTO | Java Record 사용 고려 |

### 4.3 신규 Controller 템플릿

```java
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/{entities}")
@ServiceDesc(description = "{Entity} Service API")
public class {Entity}Controller extends AbstractRestService {

    @Override
    protected Class<?> entityClass() {
        return {Entity}.class;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Search (Pagination) By Search Conditions")
    public Page<?> index(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "select", required = false) String select,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "query", required = false) String query) {
        return this.search(this.entityClass(), page, limit, select, sort, query);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Find one by ID")
    public {Entity} findOne(@PathVariable("id") String id) {
        return this.getOne(this.entityClass(), id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiDesc(description = "Create")
    public {Entity} create(@RequestBody {Entity} input) {
        return this.createOne(input);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Update")
    public {Entity} update(@PathVariable("id") String id, @RequestBody {Entity} input) {
        return this.updateOne(input);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Delete")
    public void delete(@PathVariable("id") String id) {
        this.deleteOne(this.entityClass(), id);
    }

    @PostMapping(value = "/update_multiple", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Create, Update or Delete multiple at one time")
    public Boolean multipleUpdate(@RequestBody List<{Entity}> list) {
        return this.cudMultipleData(this.entityClass(), list);
    }
}
```

### 4.4 신규 Service 템플릿

```java
/**
 * {기능} 서비스
 *
 * @author {작성자}
 */
@Service
public class {Name}Service {

    private final IQueryManager queryManager;

    public {Name}Service(IQueryManager queryManager) {
        this.queryManager = queryManager;
    }

    /**
     * {비즈니스 로직 설명}
     *
     * @param param 파라미터 설명
     * @return 반환값 설명
     */
    public Result doSomething(Param param) {
        // 1. 유효성 검사
        // 2. 비즈니스 로직
        // 3. 결과 반환
    }
}
```

---

## 5. 개선 우선순위 로드맵

### Phase 1 — 즉시 (개발 시작 전)

- [ ] 설정 파일 민감 정보 분리 (`.env` 또는 Jasypt 적용)
- [ ] `.gitignore`에 `.env`, 환경별 패스워드 파일 추가
- [ ] 이 코딩 컨벤션 문서를 팀 리뷰 및 확정

### Phase 2 — 신규 코드 적용 (개발 진행 중)

- [ ] 신규 서비스: 생성자 주입 + `@Service` 적용
- [ ] 신규 컨트롤러: 축약 어노테이션 사용
- [ ] 신규 쿼리: 서비스 계층에 위치, Text Block 사용
- [ ] Stream/Optional 적극 활용

### Phase 3 — 점진적 리팩토링 (안정화 후)

- [ ] 핵심 서비스 단위 테스트 추가
- [ ] 기존 필드 주입 → 생성자 주입 전환
- [ ] 컨트롤러 내 SQL → 서비스/QueryStore 분리
- [ ] Lombok 도입 검토 (dbist 호환성 확인 후)

### Phase 4 — 장기 개선

- [ ] Entity 비즈니스 로직 → 서비스 레이어 이동
- [ ] import 정렬 규칙 IDE 설정 통일
- [ ] 코드 포매터 설정 공유 (`.editorconfig` 또는 IDE 설정)

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 1.0 | 2026-03-01 | 초기 작성 — 1,202개 Java 파일 분석 기반 | Claude Code |
