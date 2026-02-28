# Operato WCS 백엔드 개선 체크리스트

**작성일**: 2026-03-01
**전체 평점**: 7.5/10 → **목표**: 9.0/10

이 체크리스트는 코드 품질 분석 결과를 기반으로 작성되었습니다.
각 항목을 단계적으로 완료하여 시스템 품질을 개선하세요.

---

## 📋 진행 상황 요약

| 단계 | 목표 | 예상 기간 | 현재 상태 | 목표 점수 |
|------|------|----------|----------|----------|
| Phase 1 | 보안 취약점 해결 | 1주 | ⬜ 대기 중 | 8.0/10 |
| Phase 2 | 핵심 테스트 작성 | 1개월 | ⬜ 대기 중 | 8.5/10 |
| Phase 3 | 전체 품질 개선 | 3개월 | ⬜ 대기 중 | 9.0/10 |
| Phase 4 | 고급 기능 추가 | 6개월 | ⬜ 대기 중 | 9.5/10 |

---

## 🔴 Phase 1: 보안 취약점 해결 (1주 이내)

**우선순위**: Critical
**담당자**: ___________
**목표 완료일**: ___________

### 1.1 취약한 라이브러리 업그레이드

#### commons-collections 3.2.2 제거/업그레이드
- [ ] 프로젝트 내 commons-collections 3.2.2 사용 위치 확인
  ```bash
  ./gradlew dependencies | grep commons-collections
  ```
- [ ] commons-collections4 4.4로 마이그레이션 또는 완전 제거
  ```gradle
  // build.gradle
  - implementation 'commons-collections:commons-collections:3.2.2'
  + implementation 'org.apache.commons:commons-collections4:4.4'
  ```
- [ ] 코드에서 사용 중인 API 변경 확인
  - 패키지명: `org.apache.commons.collections` → `org.apache.commons.collections4`
- [ ] 빌드 및 테스트 확인
  ```bash
  ./gradlew clean build
  ```
- [ ] CVE-2015-7501 취약점 해결 확인

#### fastjson 1.2.47 업그레이드
- [ ] fastjson 사용 위치 전체 확인
  ```bash
  grep -r "fastjson" src/
  ```
- [ ] fastjson 1.2.83 이상으로 업그레이드 또는 Jackson으로 교체
  ```gradle
  // Option 1: 업그레이드
  - implementation 'com.alibaba:fastjson:1.2.47'
  + implementation 'com.alibaba:fastjson:1.2.83'

  // Option 2: Jackson으로 교체 (권장)
  - implementation 'com.alibaba:fastjson:1.2.47'
  // Jackson은 Spring Boot에 기본 포함
  ```
- [ ] Jackson 마이그레이션 시 코드 변경
  ```java
  // Before (fastjson)
  String json = JSON.toJSONString(object);
  Object obj = JSON.parseObject(json, Object.class);

  // After (Jackson)
  ObjectMapper mapper = new ObjectMapper();
  String json = mapper.writeValueAsString(object);
  Object obj = mapper.readValue(json, Object.class);
  ```
- [ ] 모든 JSON 처리 코드 테스트

#### commons-dbcp 1.4 제거
- [ ] commons-dbcp 사용 확인
- [ ] HikariCP로 교체 (Spring Boot 기본 포함)
  ```gradle
  - implementation 'commons-dbcp:commons-dbcp:1.4'
  // HikariCP는 spring-boot-starter-jdbc에 포함됨
  ```
- [ ] application.yml에서 HikariCP 설정 확인
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        connection-timeout: 30000
  ```

#### velocity 1.7 업그레이드
- [ ] velocity 2.3으로 업그레이드
  ```gradle
  - implementation 'org.apache.velocity:velocity:1.7'
  + implementation 'org.apache.velocity:velocity-engine-core:2.3'
  ```
- [ ] API 변경 사항 확인 및 코드 수정

---

### 1.2 민감 정보 암호화

#### Jasypt 설정 (이미 의존성 있음)
- [ ] Jasypt 암호화 키 생성 및 환경 변수 설정
  ```bash
  export JASYPT_ENCRYPTOR_PASSWORD="your-secret-key-here"
  ```
- [ ] application.yml에 Jasypt 설정 추가
  ```yaml
  jasypt:
    encryptor:
      password: ${JASYPT_ENCRYPTOR_PASSWORD}
      algorithm: PBEWithMD5AndDES
  ```

#### 민감 정보 암호화
- [ ] DB 비밀번호 암호화
  ```bash
  # CLI로 암호화
  java -cp jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
       input="anythings" \
       password="${JASYPT_ENCRYPTOR_PASSWORD}" \
       algorithm=PBEWithMD5AndDES
  ```
- [ ] application-dev.properties 수정
  ```properties
  # Before
  - spring.datasource.password=anythings

  # After
  + spring.datasource.password=ENC(암호화된_값)
  ```
- [ ] 이메일 비밀번호 암호화
  ```properties
  - mail.smtp.password=1q2w3e4r~!
  + mail.smtp.password=ENC(암호화된_값)
  ```
- [ ] RabbitMQ 비밀번호 암호화
  ```properties
  - mq.broker.user.pw=admin
  + mq.broker.user.pw=ENC(암호화된_값)
  ```

#### IP 주소 환경 변수화
- [ ] 하드코딩된 IP 주소를 환경 변수로 변경
  ```properties
  # Before
  - spring.datasource.url=jdbc:oracle:thin:@60.196.69.234:20000:orcl
  - mq.broker.address=60.196.69.234

  # After
  + spring.datasource.url=jdbc:oracle:thin:@${DB_HOST}:${DB_PORT}:${DB_SID}
  + mq.broker.address=${MQ_HOST}
  ```
- [ ] 환경별 설정 파일에서 IP 주소 제거

---

### 1.3 Spring Security 강화

#### 권한 검증 활성화
- [ ] SecurityConfigration.java 수정
  ```java
  http.authorizeHttpRequests()
      // Public 엔드포인트
      .requestMatchers("/rest/login", "/rest/refresh", "/actuator/health").permitAll()
      // 관리자 전용
      .requestMatchers("/rest/admin/**").hasRole("ADMIN")
      // 인증 필수
      .requestMatchers("/rest/**").authenticated()
      // 기타 모든 요청 거부
      .anyRequest().denyAll();
  ```

#### JWT 인증 필터 활성화
- [ ] JwtAuthenticationFilter 구현 완성
- [ ] SecurityFilterChain에 필터 추가
  ```java
  .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
  ```
- [ ] JWT 토큰 검증 로직 테스트

#### 보안 헤더 추가
- [ ] CSP, X-Frame-Options 등 보안 헤더 설정
  ```java
  .headers()
      .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline';")
      .and()
      .xssProtection()
      .and()
      .frameOptions().deny()
      .and()
      .contentTypeOptions();
  ```

#### CORS 설정
- [ ] WebConfig.java에 명시적 CORS 설정 추가
  ```java
  @Override
  public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/rest/**")
              .allowedOrigins("https://your-frontend-domain.com")
              .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
              .allowedHeaders("*")
              .allowCredentials(true)
              .maxAge(3600);
  }
  ```

---

### 1.4 보안 검증

- [ ] 의존성 취약점 스캔 실행
  ```bash
  ./gradlew dependencyCheckAnalyze
  ```
- [ ] OWASP Top 10 체크리스트 확인
- [ ] 민감 정보 노출 여부 재확인
  ```bash
  grep -r "password\|secret\|key" src/main/resources/ --include="*.properties" --include="*.yml"
  ```
- [ ] Git 커밋 히스토리에서 민감 정보 제거
  ```bash
  git filter-branch --force --index-filter \
    "git rm --cached --ignore-unmatch src/main/resources/application-dev.properties" \
    --prune-empty --tag-name-filter cat -- --all
  ```

---

## 🟠 Phase 2: 핵심 테스트 작성 (1개월)

**우선순위**: High
**담당자**: ___________
**목표 완료일**: ___________
**목표 커버리지**: 50%

### 2.1 테스트 환경 구축

#### 프로젝트 구조 생성
- [ ] `src/test/java` 디렉토리 생성
- [ ] `src/test/resources` 디렉토리 생성
- [ ] 패키지 구조 복사 (main과 동일하게)

#### build.gradle에 테스트 의존성 추가
- [ ] JUnit 5 의존성 추가
  ```gradle
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testImplementation 'org.junit.jupiter:junit-jupiter-api'
  testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
  ```
- [ ] Mockito 의존성 추가
  ```gradle
  testImplementation 'org.mockito:mockito-core'
  testImplementation 'org.mockito:mockito-junit-jupiter'
  ```
- [ ] AssertJ 의존성 추가
  ```gradle
  testImplementation 'org.assertj:assertj-core'
  ```
- [ ] H2 테스트 DB 추가
  ```gradle
  testImplementation 'com.h2database:h2'
  ```
- [ ] Testcontainers 추가
  ```gradle
  testImplementation 'org.testcontainers:testcontainers'
  testImplementation 'org.testcontainers:junit-jupiter'
  testImplementation 'org.testcontainers:postgresql'
  ```

#### Jacoco 플러그인 설정
- [ ] build.gradle에 Jacoco 플러그인 추가
  ```gradle
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
      }
  }
  ```

#### 테스트 환경 설정
- [ ] `application-test.properties` 생성
  ```properties
  spring.profiles.active=test
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.jpa.hibernate.ddl-auto=create-drop
  mq.enabled=false
  ```

---

### 2.2 핵심 Service 계층 테스트 (우선순위 높음)

#### InstructionService 테스트
- [ ] `InstructionServiceTest.java` 생성
- [ ] 작업 지시 생성 테스트
- [ ] 작업 지시 취소 테스트
- [ ] 작업 지시 병합 테스트
- [ ] 예외 상황 테스트 (null batch, empty equipList 등)
- [ ] 테스트 커버리지 70% 이상 달성

#### BatchService 테스트
- [ ] `BatchServiceTest.java` 생성
- [ ] 배치 생성 테스트
- [ ] 배치 상태 변경 테스트
- [ ] 배치 병합 테스트
- [ ] 배치 종료 테스트
- [ ] 테스트 커버리지 70% 이상 달성

#### OrderService 테스트
- [ ] `OrderServiceTest.java` 생성
- [ ] 주문 생성 테스트
- [ ] 주문 조회 테스트
- [ ] 주문 수정 테스트
- [ ] 주문 삭제 테스트
- [ ] 테스트 커버리지 70% 이상 달성

#### StockService 테스트
- [ ] `StockServiceTest.java` 생성
- [ ] 재고 조회 테스트
- [ ] 재고 이동 테스트
- [ ] 재고 조정 테스트
- [ ] 테스트 커버리지 70% 이상 달성

#### GatewayService 테스트
- [ ] `GatewayServiceTest.java` 생성
- [ ] Gateway 연동 테스트
- [ ] 메시지 발행 테스트
- [ ] 메시지 수신 테스트
- [ ] 테스트 커버리지 70% 이상 달성

---

### 2.3 REST API 통합 테스트

#### OrderController 통합 테스트
- [ ] `OrderControllerIntegrationTest.java` 생성
- [ ] 주문 목록 조회 API 테스트
- [ ] 주문 생성 API 테스트
- [ ] 주문 수정 API 테스트
- [ ] 주문 삭제 API 테스트
- [ ] 404, 400 등 예외 상황 테스트

#### BatchController 통합 테스트
- [ ] `BatchControllerIntegrationTest.java` 생성
- [ ] 배치 목록 조회 API 테스트
- [ ] 배치 생성 API 테스트
- [ ] 배치 작업 지시 API 테스트
- [ ] 배치 종료 API 테스트

#### EquipmentController 통합 테스트
- [ ] `EquipmentControllerIntegrationTest.java` 생성
- [ ] 설비 목록 조회 API 테스트
- [ ] 설비 상태 변경 API 테스트
- [ ] 설비 설정 API 테스트

---

### 2.4 Entity 및 Repository 테스트

#### JobBatch Entity 테스트
- [ ] `JobBatchTest.java` 생성
- [ ] Entity 생성 및 저장 테스트
- [ ] Entity 조회 테스트
- [ ] Entity 관계 매핑 테스트

#### Order Entity 테스트
- [ ] `OrderTest.java` 생성
- [ ] Entity CRUD 테스트

---

### 2.5 RabbitMQ 통합 테스트

#### GatewayMessageService 테스트
- [ ] `GatewayMessageServiceTest.java` 생성
- [ ] Testcontainers RabbitMQ 설정
- [ ] 메시지 발행 테스트
- [ ] 메시지 수신 테스트
- [ ] 메시지 라우팅 테스트

---

### 2.6 테스트 커버리지 확인

- [ ] 전체 테스트 실행
  ```bash
  ./gradlew test
  ```
- [ ] 커버리지 리포트 생성
  ```bash
  ./gradlew jacocoTestReport
  ```
- [ ] HTML 리포트 확인
  ```bash
  open build/reports/jacoco/test/html/index.html
  ```
- [ ] 50% 커버리지 달성 확인
- [ ] 커버리지 미달 영역 파악 및 추가 테스트 작성

---

## 🟡 Phase 3: 전체 품질 개선 (3개월)

**우선순위**: Medium
**담당자**: ___________
**목표 완료일**: ___________

### 3.1 로깅 강화

#### 비즈니스 로깅 추가
- [ ] 주요 Service 클래스에 logger 필드 추가
  ```java
  private static final Logger logger = LoggerFactory.getLogger(InstructionService.class);
  ```
- [ ] 핵심 비즈니스 로직에 로깅 추가
  ```java
  logger.info("작업 지시 생성 시작: batchId={}, equipCount={}", batch.getId(), equipIdList.size());
  ```

#### RabbitMQ 메시지 로깅
- [ ] 메시지 발행 시 로깅
  ```java
  logger.debug("RabbitMQ 메시지 발행: exchange={}, routingKey={}, message={}",
               exchange, routingKey, message);
  ```
- [ ] 메시지 수신 시 로깅
  ```java
  logger.debug("RabbitMQ 메시지 수신: message={}", message);
  ```

#### 성능 로깅
- [ ] API 응답 시간 로깅 AOP 추가
  ```java
  @Around("execution(* xyz.anythings.base.rest..*.*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
      long start = System.currentTimeMillis();
      Object result = joinPoint.proceed();
      long executionTime = System.currentTimeMillis() - start;
      logger.info("{} 실행 시간: {}ms", joinPoint.getSignature(), executionTime);
      return result;
  }
  ```

#### 감사(Audit) 로깅
- [ ] 중요 작업 이력 로깅
  - 주문 생성/수정/삭제
  - 작업 지시 생성/취소
  - 배치 시작/종료
  - 설비 상태 변경
- [ ] 감사 로그 별도 파일 저장
  ```xml
  <appender name="auditFileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
      <file>logs/audit.log</file>
  </appender>
  ```

---

### 3.2 미완성 코드 완성

#### TODO/FIXME 주석 해결
- [ ] 표시기 1000개 이상 처리 쿼리 최적화
  ```java
  // TODO 쿼리로 수정 필요 - 표시기 개수가 1000개 이상인 경우 에러 발생
  // → IN 절 대신 배치 처리 또는 임시 테이블 활용
  ```
- [ ] 재고 이력 관리 로직 완성
  ```java
  // TODO 설정에 존재하는 재고 이력 관리할 트랜잭션 리스트에 포함되어 있는지 체크 후 이력 추가
  ```
- [ ] 기타 설비 추가
  ```java
  // TODO 기타 설비 추가 필요함
  ```
- [ ] gwPath 조회 로직 완성
  ```java
  // FIXME gwPath 조회
  ```
- [ ] 분기 처리 개선
  ```java
  // FIXME 아래 분기하는 것 외 다른 방법 찾기
  // → Strategy 패턴 적용
  ```

#### Auto-generated stub 제거
- [ ] StockService 구현 완성
- [ ] 기타 미구현 메서드 완성
- [ ] 모든 "TODO Auto-generated method stub" 제거

---

### 3.3 문서화 개선

#### Swagger/OpenAPI 통합
- [ ] springdoc-openapi 의존성 추가
  ```gradle
  implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
  ```
- [ ] Swagger 설정 클래스 작성
  ```java
  @Configuration
  public class OpenApiConfig {
      @Bean
      public OpenAPI customOpenAPI() {
          return new OpenAPI()
                  .info(new Info()
                          .title("Operato WCS API")
                          .version("1.0")
                          .description("WCS REST API 문서"));
      }
  }
  ```
- [ ] Controller에 @Operation, @ApiResponse 어노테이션 추가
- [ ] Swagger UI 확인: http://localhost:8080/swagger-ui.html

#### JavaDoc 완성
- [ ] 모든 public 클래스에 JavaDoc 추가
- [ ] 모든 public 메서드에 JavaDoc 추가
  ```java
  /**
   * 작업 배치에 대한 작업 지시를 생성합니다.
   *
   * @param batch 작업 배치 정보
   * @param equipIdList 설비 ID 리스트
   * @param params 추가 파라미터
   * @return 생성된 작업 지시 수
   * @throws IllegalArgumentException batch가 null인 경우
   */
  public int instructBatch(JobBatch batch, List<String> equipIdList, Object... params) {
      // ...
  }
  ```
- [ ] JavaDoc 생성
  ```bash
  ./gradlew javadoc
  ```

#### API 문서 작성
- [ ] `docs/api/REST-API.md` 작성
- [ ] 각 엔드포인트별 요청/응답 예시 추가
- [ ] 에러 코드 정의

---

### 3.4 테스트 커버리지 80% 달성

#### 추가 Service 계층 테스트
- [ ] 나머지 모든 Service 클래스 테스트 작성
- [ ] Edge case 테스트 추가
- [ ] 예외 상황 테스트 추가

#### 추가 Controller 통합 테스트
- [ ] 나머지 모든 Controller 통합 테스트 작성
- [ ] 권한 검증 테스트 추가
- [ ] 입력 검증 테스트 추가

#### E2E 테스트 추가
- [ ] 주요 비즈니스 플로우 E2E 테스트
  - 입고 → 보관 → 출고 플로우
  - 주문 수신 → 작업 지시 → 실적 보고 플로우

---

## 🟢 Phase 4: 고급 기능 추가 (6개월)

**우선순위**: Low
**담당자**: ___________
**목표 완료일**: ___________

### 4.1 모니터링 강화

#### Spring Actuator 활성화
- [ ] Actuator 의존성 추가
  ```gradle
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
  ```
- [ ] application.yml 설정
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus
    endpoint:
      health:
        show-details: always
  ```
- [ ] Health Check 엔드포인트 확인: `/actuator/health`

#### Prometheus/Grafana 연동
- [ ] Micrometer Prometheus 의존성 추가
  ```gradle
  implementation 'io.micrometer:micrometer-registry-prometheus'
  ```
- [ ] Prometheus 설정 파일 작성 (`prometheus.yml`)
- [ ] Grafana 대시보드 구성
- [ ] 주요 메트릭 모니터링
  - API 응답 시간
  - JVM 메모리 사용량
  - DB Connection Pool 상태
  - RabbitMQ 큐 길이

#### 커스텀 메트릭 추가
- [ ] 비즈니스 메트릭 정의
  - 시간당 처리 주문 수
  - 작업 지시 처리 시간
  - 설비 가동율
- [ ] Micrometer로 메트릭 수집
  ```java
  @Autowired
  private MeterRegistry meterRegistry;

  public void processOrder(Order order) {
      meterRegistry.counter("orders.processed").increment();
      Timer.Sample sample = Timer.start(meterRegistry);
      // 주문 처리 로직
      sample.stop(meterRegistry.timer("orders.processing.time"));
  }
  ```

---

### 4.2 코드 품질 도구 도입

#### SonarQube 정적 분석
- [ ] SonarQube 서버 구축 또는 SonarCloud 가입
- [ ] build.gradle에 SonarQube 플러그인 추가
  ```gradle
  plugins {
      id 'org.sonarqube' version '4.4.1.3373'
  }

  sonarqube {
      properties {
          property 'sonar.projectKey', 'operato-wcs'
          property 'sonar.host.url', 'http://localhost:9000'
      }
  }
  ```
- [ ] SonarQube 분석 실행
  ```bash
  ./gradlew sonarqube
  ```
- [ ] Code Smell, 버그, 취약점 확인 및 수정

#### Checkstyle 도입
- [ ] build.gradle에 Checkstyle 플러그인 추가
  ```gradle
  plugins {
      id 'checkstyle'
  }

  checkstyle {
      toolVersion = '10.12.5'
      configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
  }
  ```
- [ ] Google Java Style Guide 적용
- [ ] Checkstyle 실행
  ```bash
  ./gradlew checkstyleMain
  ```

#### PMD 도입
- [ ] build.gradle에 PMD 플러그인 추가
  ```gradle
  plugins {
      id 'pmd'
  }

  pmd {
      toolVersion = '6.55.0'
      ruleSets = []
      ruleSetFiles = files("${rootDir}/config/pmd/ruleset.xml")
  }
  ```
- [ ] PMD 실행
  ```bash
  ./gradlew pmdMain
  ```

---

### 4.3 CI/CD 파이프라인 구축

#### GitHub Actions 워크플로우
- [ ] `.github/workflows/build.yml` 작성
  ```yaml
  name: Build and Test

  on:
    push:
      branches: [ main, develop ]
    pull_request:
      branches: [ main, develop ]

  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - name: Set up JDK 17
          uses: actions/setup-java@v3
          with:
            java-version: '17'
            distribution: 'temurin'
        - name: Build with Gradle
          run: ./gradlew build
        - name: Run tests
          run: ./gradlew test
        - name: Generate coverage
          run: ./gradlew jacocoTestReport
        - name: Upload coverage to Codecov
          uses: codecov/codecov-action@v3
  ```

#### 자동 배포 파이프라인
- [ ] `.github/workflows/deploy.yml` 작성
- [ ] Docker 이미지 빌드 및 푸시
- [ ] Kubernetes 또는 Docker Compose 배포

---

### 4.4 성능 최적화

#### 데이터베이스 쿼리 최적화
- [ ] Slow Query 로깅 활성화
- [ ] N+1 쿼리 문제 해결 (Eager Loading, Fetch Join)
- [ ] 인덱스 최적화
- [ ] 쿼리 실행 계획 분석

#### 캐싱 전략 구현
- [ ] Redis 캐싱 적용
  - 마스터 데이터 캐싱
  - 설비 상태 캐싱
  - 설정 정보 캐싱
- [ ] Spring Cache 추상화 활용
  ```java
  @Cacheable(value = "equipment", key = "#equipId")
  public Equipment findEquipment(String equipId) {
      // DB 조회
  }
  ```

#### 비동기 처리
- [ ] @Async 활용
  - 이메일 발송 비동기 처리
  - 로그 저장 비동기 처리
- [ ] CompletableFuture 활용
- [ ] 스레드 풀 설정 최적화

---

## 📊 진행 상황 추적

### 주간 체크포인트

**Week 1**: ___________
- [ ] Phase 1.1 완료 (취약한 라이브러리 업그레이드)
- [ ] Phase 1.2 완료 (민감 정보 암호화)
- [ ] Phase 1.3 완료 (Spring Security 강화)
- [ ] Phase 1.4 완료 (보안 검증)

**Week 2-4**: ___________
- [ ] Phase 2.1 완료 (테스트 환경 구축)
- [ ] Phase 2.2 완료 (핵심 Service 테스트)
- [ ] 30% 커버리지 달성

**Month 2**: ___________
- [ ] Phase 2.3 완료 (REST API 통합 테스트)
- [ ] Phase 2.4 완료 (Entity 테스트)
- [ ] Phase 2.5 완료 (RabbitMQ 테스트)
- [ ] 50% 커버리지 달성

**Month 3**: ___________
- [ ] Phase 3.1 완료 (로깅 강화)
- [ ] Phase 3.2 완료 (미완성 코드 완성)
- [ ] Phase 3.3 완료 (문서화 개선)
- [ ] Phase 3.4 완료 (80% 커버리지)

**Month 4-6**: ___________
- [ ] Phase 4.1 완료 (모니터링 강화)
- [ ] Phase 4.2 완료 (코드 품질 도구)
- [ ] Phase 4.3 완료 (CI/CD 파이프라인)
- [ ] Phase 4.4 완료 (성능 최적화)

---

## 🎯 최종 검증

### 품질 목표 달성 확인

- [ ] 전체 코드 품질 점수: 9.0/10 이상
- [ ] 보안 점수: 9.0/10 이상
- [ ] 테스트 커버리지: 80% 이상
- [ ] 모든 TODO/FIXME 주석 해결
- [ ] 모든 보안 취약점 해결
- [ ] API 문서 완성
- [ ] 모니터링 대시보드 구축

### 프로덕션 준비 체크리스트

- [ ] 모든 테스트 통과
- [ ] 성능 테스트 완료
- [ ] 보안 감사 완료
- [ ] 문서화 완료
- [ ] 운영 가이드 작성
- [ ] 롤백 계획 수립
- [ ] 모니터링 알림 설정
- [ ] 백업 전략 수립

---

**마지막 업데이트**: 2026-03-01
**담당자**: ___________
**승인자**: ___________
