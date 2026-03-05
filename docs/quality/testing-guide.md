# 테스트 코드 작성 가이드

## 현재 상태

**현재 테스트 코드: 0개**
- `src/test` 디렉토리 없음
- 코드 커버리지: 0%
- 회귀 테스트 불가능

## 목표

**단계별 테스트 커버리지 목표**
- Phase 1 (1개월): 핵심 비즈니스 로직 50% 커버리지
- Phase 2 (2개월): 전체 Service 계층 70% 커버리지
- Phase 3 (3개월): 전체 프로젝트 80% 커버리지

---

## 1. 프로젝트 구조 생성

### 디렉토리 구조
```
src/
├── main/
│   └── java/
│       └── xyz/
│           ├── anythings/
│           └── elidom/
└── test/
    ├── java/
    │   └── xyz/
    │       ├── anythings/
    │       │   ├── base/
    │       │   │   ├── service/        # 서비스 단위 테스트
    │       │   │   ├── rest/           # Controller 테스트
    │       │   │   └── entity/         # Entity 테스트
    │       │   └── gw/
    │       │       ├── service/
    │       │       └── rest/
    │       └── elidom/
    │           └── sys/
    └── resources/
        ├── application-test.properties
        └── test-data/
            └── sample-data.sql
```

### build.gradle에 테스트 의존성 추가

```gradle
dependencies {
    // 기존 의존성...

    // 테스트 프레임워크
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'

    // Mockito (Spring Boot Test에 포함되어 있지만 명시적 추가)
    testImplementation 'org.mockito:mockito-core'
    testImplementation 'org.mockito:mockito-junit-jupiter'

    // AssertJ (더 나은 assertion)
    testImplementation 'org.assertj:assertj-core'

    // 테스트용 DB (H2)
    testImplementation 'com.h2database:h2'

    // REST API 테스트
    testImplementation 'io.rest-assured:rest-assured'
    testImplementation 'io.rest-assured:json-path'

    // Testcontainers (통합 테스트용)
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'

    // 코드 커버리지
    // Jacoco는 plugin으로 추가
}

// Jacoco 플러그인 설정
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.11"
}

test {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/entity/**',
                '**/config/**',
                '**/Application.class'
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.70  // 70% 커버리지 목표
            }
        }
    }
}
```

---

## 2. 테스트 환경 설정

### application-test.properties

```properties
# 테스트 환경 설정
spring.profiles.active=test

# H2 인메모리 DB (빠른 테스트)
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 로깅
logging.level.xyz.anythings=DEBUG
logging.level.xyz.elidom=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# RabbitMQ (테스트 시 비활성화 또는 Embedded)
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
mq.enabled=false

# Redis (테스트 시 Embedded 사용)
spring.redis.host=localhost
spring.redis.port=6379
```

---

## 3. 단위 테스트 작성 예시

### Service 계층 테스트

#### InstructionServiceTest.java
```java
package xyz.anythings.base.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import xyz.anythings.base.entity.JobBatch;
import xyz.elidom.orm.IQueryManager;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("작업 지시 서비스 테스트")
class InstructionServiceTest {

    @Mock
    private IQueryManager queryManager;

    @Mock
    private BatchService batchService;

    @InjectMocks
    private InstructionService instructionService;

    private JobBatch testBatch;

    @BeforeEach
    void setUp() {
        testBatch = new JobBatch();
        testBatch.setId("BATCH-001");
        testBatch.setStatus("RUNNING");
        testBatch.setJobType("PICKING");
    }

    @Test
    @DisplayName("배치에 대한 작업 지시 생성 성공")
    void testInstructBatch_Success() {
        // Given
        List<String> equipIdList = Arrays.asList("EQUIP-001", "EQUIP-002");
        when(batchService.findById(anyString())).thenReturn(testBatch);

        // When
        int result = instructionService.instructBatch(testBatch, equipIdList);

        // Then
        assertThat(result).isGreaterThan(0);
        verify(queryManager, atLeastOnce()).select(any());
    }

    @Test
    @DisplayName("배치가 null인 경우 예외 발생")
    void testInstructBatch_NullBatch() {
        // Given
        List<String> equipIdList = Arrays.asList("EQUIP-001");

        // When & Then
        assertThatThrownBy(() -> instructionService.instructBatch(null, equipIdList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batch");
    }

    @Test
    @DisplayName("작업 지시 취소 성공")
    void testCancelInstruction_Success() {
        // Given
        when(batchService.findById(anyString())).thenReturn(testBatch);

        // When
        instructionService.cancelInstructionBatch(testBatch);

        // Then
        verify(queryManager, times(1)).update(any(), any());
    }
}
```

### Repository 계층 테스트 (Entity 테스트)

#### JobBatchTest.java
```java
package xyz.anythings.base.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("JobBatch Entity 테스트")
class JobBatchTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("JobBatch 생성 및 저장")
    void testCreateAndSave() {
        // Given
        JobBatch batch = new JobBatch();
        batch.setDomainId(1L);
        batch.setBatchId("BATCH-001");
        batch.setJobType("PICKING");
        batch.setStatus("RUNNING");

        // When
        JobBatch saved = entityManager.persistAndFlush(batch);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBatchId()).isEqualTo("BATCH-001");
        assertThat(saved.getJobType()).isEqualTo("PICKING");
    }

    @Test
    @DisplayName("JobBatch 조회")
    void testFind() {
        // Given
        JobBatch batch = new JobBatch();
        batch.setDomainId(1L);
        batch.setBatchId("BATCH-002");
        JobBatch saved = entityManager.persistAndFlush(batch);

        // When
        JobBatch found = entityManager.find(JobBatch.class, saved.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getBatchId()).isEqualTo("BATCH-002");
    }
}
```

---

## 4. 통합 테스트 작성 예시

### REST API 통합 테스트

#### OrderControllerIntegrationTest.java
```java
package xyz.anythings.base.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Order API 통합 테스트")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void testGetOrderList() throws Exception {
        mockMvc.perform(get("/rest/orders")
                .param("page", "1")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("주문 생성 API")
    void testCreateOrder() throws Exception {
        String orderJson = """
                {
                    "orderNo": "ORD-001",
                    "orderType": "PICKING",
                    "status": "READY"
                }
                """;

        mockMvc.perform(post("/rest/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").value("ORD-001"));
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 404")
    void testGetNonExistentOrder() throws Exception {
        mockMvc.perform(get("/rest/orders/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
```

### Testcontainers를 활용한 PostgreSQL 통합 테스트

#### OrderServiceIntegrationTest.java
```java
package xyz.anythings.base.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Order 서비스 통합 테스트 (Testcontainers)")
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("PostgreSQL 통합 테스트")
    void testOrderCreationWithRealDB() {
        // Given
        // Order 생성 로직

        // When
        // orderService.createOrder(...)

        // Then
        // assertThat(...)
    }
}
```

---

## 5. RabbitMQ 통합 테스트

### RabbitMQ 메시지 발행/구독 테스트

#### GatewayMessageServiceTest.java
```java
package xyz.anythings.gw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Gateway 메시지 서비스 테스트")
class GatewayMessageServiceTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management");

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("RabbitMQ 메시지 발행 테스트")
    void testPublishMessage() {
        // Given
        String message = "{\"type\": \"indicator\", \"action\": \"light_on\"}";

        // When
        rabbitTemplate.convertAndSend("wcs.exchange", "indicator.command", message);

        // Then
        // 메시지 수신 확인
    }
}
```

---

## 6. 테스트 실행 및 커버리지 확인

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests InstructionServiceTest

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# 커버리지 검증
./gradlew jacocoTestCoverageVerification
```

### 커버리지 리포트 확인
```bash
# HTML 리포트 열기
open build/reports/jacoco/test/html/index.html

# 또는
open build/reports/tests/test/index.html
```

---

## 7. CI/CD 통합

### GitHub Actions 예시 (.github/workflows/test.yml)

```yaml
name: Run Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run tests
        run: ./gradlew test

      - name: Generate coverage report
        run: ./gradlew jacocoTestReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: build/reports/jacoco/test/jacocoTestReport.xml

      - name: Publish test results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: build/test-results/**/*.xml
```

---

## 8. 우선순위별 테스트 작성 계획

### Phase 1: 핵심 비즈니스 로직 (1개월)
- [ ] InstructionService 테스트
- [ ] BatchService 테스트
- [ ] OrderService 테스트
- [ ] StockService 테스트
- [ ] GatewayService 테스트

### Phase 2: REST API 및 통합 테스트 (2개월)
- [ ] OrderController 통합 테스트
- [ ] BatchController 통합 테스트
- [ ] EquipmentController 통합 테스트
- [ ] RabbitMQ 메시지 통합 테스트

### Phase 3: 전체 커버리지 향상 (3개월)
- [ ] 모든 Service 계층 테스트 완성
- [ ] 모든 Controller 통합 테스트 완성
- [ ] E2E 테스트 추가
- [ ] 성능 테스트 추가

---

## 9. 테스트 베스트 프랙티스

### 명명 규칙
```java
// ✅ 좋은 예
@Test
@DisplayName("배치가 null인 경우 예외 발생")
void testInstructBatch_NullBatch() { ... }

// ❌ 나쁜 예
@Test
void test1() { ... }
```

### Given-When-Then 패턴 사용
```java
@Test
void testExample() {
    // Given (준비)
    JobBatch batch = new JobBatch();

    // When (실행)
    int result = service.process(batch);

    // Then (검증)
    assertThat(result).isEqualTo(1);
}
```

### 테스트는 독립적이어야 함
```java
// ✅ 좋은 예 - BeforeEach에서 초기화
@BeforeEach
void setUp() {
    testBatch = new JobBatch();
}

// ❌ 나쁜 예 - 테스트 간 상태 공유
static JobBatch sharedBatch;  // 여러 테스트가 공유
```

---

## 10. 참고 자료

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers](https://www.testcontainers.org/)
- [AssertJ](https://assertj.github.io/doc/)
