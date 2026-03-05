# 테스트 가이드

> 분석일: 2026-03-05 | 대상: operato-wcs-ai

---

## 1. 현재 상태

| 항목 | 상태 |
|------|------|
| 단위 테스트 | 0개 |
| 통합 테스트 | 0개 |
| `src/test/` 디렉토리 | 미존재 |
| 테스트 프레임워크 | build.gradle에 spring-boot-starter-test 포함 |

테스트 코드가 전무하여 **가장 시급한 품질 개선 항목**입니다.

---

## 2. 테스트 환경 구성

### 2.1 디렉토리 생성

```bash
mkdir -p src/test/java/operato/logis
mkdir -p src/test/resources
```

### 2.2 의존성 확인

`build.gradle`에 이미 포함된 테스트 의존성:
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

추가 권장:
```gradle
testImplementation 'org.mockito:mockito-core'
testImplementation 'org.assertj:assertj-core'
```

---

## 3. 테스트 전략

### 3.1 우선순위별 테스트 대상

**P0 — 핵심 비즈니스 로직**

| 대상 | 테스트 유형 | 이유 |
|------|-----------|------|
| DasAssortService | 단위 테스트 | 분류 로직의 정확성 검증 |
| DpcPickingService | 단위 테스트 | 피킹 워크플로우 검증 |
| DpsInstructionService | 단위 테스트 | 작업 지시 처리 검증 |
| BoxingService 구현체 | 단위 테스트 | 박싱 로직 검증 |

**P1 — REST API**

| 대상 | 테스트 유형 | 이유 |
|------|-----------|------|
| OrderController | 통합 테스트 | 주문 CRUD 검증 |
| DeviceProcessController | 통합 테스트 | 디바이스 프로세스 API 검증 |
| BatchController | 통합 테스트 | 배치 관리 API 검증 |

**P2 — 인프라/이벤트**

| 대상 | 테스트 유형 | 이유 |
|------|-----------|------|
| Event Handler | 단위 테스트 | 이벤트 처리 로직 검증 |
| QueryStore | 통합 테스트 | 쿼리 정확성 검증 |
| MQ 메시지 처리 | 통합 테스트 | 메시지 송수신 검증 |

### 3.2 테스트 패턴

**서비스 단위 테스트 예시**:

```java
@ExtendWith(MockitoExtension.class)
class DasAssortServiceTest {

    @InjectMocks
    private DasAssortService dasAssortService;

    @Mock
    private IQueryManager queryManager;

    @Mock
    private DasBoxingService boxService;

    @Mock
    private ICustomService customService;

    @Test
    void classCellMapping_유효하지_않은_분류코드_예외발생() {
        // given
        JobBatch batch = createTestBatch();
        when(queryManager.selectList(eq(Order.class), any()))
            .thenReturn(Collections.emptyList());

        // when & then
        assertThrows(ElidomValidationException.class,
            () -> dasAssortService.classCellMapping(batch, "C001", "CLASS01"));
    }
}
```

**컨트롤러 통합 테스트 예시**:

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void index_페이지네이션_정상동작() throws Exception {
        mockMvc.perform(get("/rest/orders")
                .param("page", "1")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list").isArray());
    }
}
```

---

## 4. 목표 커버리지

| 단계 | 목표 | 대상 |
|------|------|------|
| 1단계 | 20% | 핵심 서비스 로직 (DAS, DPS, DPC) |
| 2단계 | 40% | REST API + 이벤트 핸들러 |
| 3단계 | 60% | 유틸리티 + QueryStore |

---

## 5. 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스 테스트
./gradlew test --tests "operato.logis.das.service.impl.DasAssortServiceTest"

# 테스트 보고서 확인
open build/reports/tests/test/index.html
```

---

## 6. 현재 제약 사항

### 필드 주입 문제

현재 모든 서비스가 `@Autowired` 필드 주입을 사용하므로, 단위 테스트에서 Mock 주입이 어렵습니다.

**단기 대안**: `@InjectMocks` + `@Mock` 조합 (Mockito가 리플렉션으로 주입)
**장기 해결**: 생성자 주입으로 전환 후 직접 Mock 전달

### DBIST ORM

JPA가 아닌 커스텀 ORM(DBIST)을 사용하므로 `@DataJpaTest` 등 Spring 표준 테스트 슬라이스를 활용할 수 없습니다.
- IQueryManager를 Mock 처리하여 테스트
- 통합 테스트 시 실제 DB 연결 필요
