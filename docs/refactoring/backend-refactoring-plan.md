# Operato WCS AI 리팩토링 계획

> **작성일**: 2026-03-01
> **작성자**: Claude Sonnet 4.5
> **기반 분석**: 코드베이스 1,200+ Java 파일 전수 분석

---

## Executive Summary

Operato WCS AI 백엔드 코드베이스(1,200+ Java 파일)를 분석한 결과, 다음과 같은 주요 문제점들이 발견되었습니다:

### 즉시 해결이 필요한 문제 (P0)
- **Bean 충돌 4건**: PrinterController 2개, TraceDeleteManager 2개
- **보안 위험**: application.properties에 하드코딩된 credentials
- **취약 라이브러리**: fastjson 1.2.47 (알려진 보안 취약점), Groovy 5.0.0-alpha-4
- **엔티티 중복**: Printer 클래스 2개가 동일 테이블 사용

### 구조적 문제 (P1-P2)
- **테스트 인프라 부재**: 커버리지 0%
- **주석 처리된 코드**: 컨트롤러 전체, 100+ 라인 메서드
- **거대 클래스**: 1,600+ 라인 컨트롤러, 1,400+ 라인 서비스
- **중복 의존성**: 4개 JSON 라이브러리 병존
- **TODO/FIXME**: 325+ 건 (stub 메서드 포함)
- **Deprecated 패키지**: javax.* (jakarta.* 마이그레이션 필요)

**목표**: 런타임 안정성 확보, 보안 컴플라이언스 준수, 유지보수성 향상

---

## Phase 0: 긴급 수정 (P0) - 1주

### 목표
빌드 안정화, 보안 위험 제거, Bean 충돌 해결

---

### 0.1 보안 위험 제거 (1일)

**문제**: `application.properties`에 평문 저장된 credentials

```properties
mail.smtp.password=1q2w3e4r~!
mq.broker.user.pw=admin
```

**해결**:
1. 민감 정보를 환경변수로 분리
2. `application-local.properties.example` 템플릿 생성
3. `.gitignore`에 `application-local.properties` 추가

**영향 파일**:
- `/src/main/resources/application*.properties` (6개 프로파일)
- `/.gitignore`

**검증**:
```bash
grep -r "password=" src/main/resources/application*.properties  # 0건이어야 함
```

---

### 0.2 취약 라이브러리 제거/업데이트 (1일)

**문제**:
- `fastjson:1.2.47` - 알려진 RCE 취약점 (CVE-2022-25845)
- `groovy-jsr223:5.0.0-alpha-4` - 알파 버전 (프로덕션 부적합)
- `commons-collections:3.2.2` - 2003년 버전 (deprecated)

**해결**:
```gradle
// build.gradle
- implementation 'com.alibaba:fastjson:1.2.47'  // 삭제
- implementation 'commons-collections:commons-collections:3.2.2'  // 삭제
- implementation 'org.codehaus.groovy:groovy-jsr223:5.0.0-alpha-4'  // 삭제

+ implementation 'org.apache.commons:commons-collections4:4.4'
+ implementation 'org.codehaus.groovy:groovy-jsr223:4.0.18'
```

**영향 파일**:
- `/build.gradle`

**영향 분석 필요**:
- fastjson 사용처 검색 후 Jackson으로 마이그레이션

**검증**:
```bash
./gradlew dependencies | grep -E "fastjson|groovy-jsr223|commons-collections"
./gradlew clean build
```

---

### 0.3 Bean 충돌 해결 - PrinterController (1일)

**문제**:
```
xyz.anythings.base.rest.PrinterController (활성)
xyz.elidom.print.rest.PrinterController (주석 처리됨)
```

**해결**:
1. `xyz.elidom.print.rest.PrinterController.java` 파일 삭제 (이미 주석 처리되어 사용 안 함)
2. `xyz.anythings.base.rest.PrinterController`만 유지

**영향 파일**:
- `/src/main/java/xyz/elidom/print/rest/PrinterController.java` - 삭제
- `/src/main/java/xyz/elidom/print/rest/PrintoutController.java` - 함께 검토 (동일 패턴)

**검증**:
```bash
./gradlew bootRun  # Bean 충돌 오류 사라짐
curl http://localhost:9500/rest/printers  # API 정상 동작
```

---

### 0.4 Bean 충돌 해결 - TraceDeleteManager (2일)

**문제**:
```
xyz.elidom.rabbitmq.logger.TraceDeleteManager (구버전)
xyz.elidom.mw.rabbitmq.logger.TraceDeleteManager (신버전, 개선된 에러 처리)
```

**해결**:
1. 두 클래스 기능 비교 분석
2. `xyz.elidom.rabbitmq.logger.TraceDeleteManager` 삭제
3. 모든 import를 `xyz.elidom.mw.rabbitmq.logger.TraceDeleteManager`로 변경

**영향 파일**:
- `/src/main/java/xyz/elidom/rabbitmq/logger/TraceDeleteManager.java` - 삭제
- 참조하는 모든 파일 import 수정

**검증**:
```bash
grep -r "xyz.elidom.rabbitmq.logger.TraceDeleteManager" src/main/java  # 0건
./gradlew bootRun  # Bean 충돌 오류 사라짐
```

---

### 0.5 엔티티 중복 해결 - Printer (2일)

**문제**:
```
xyz.anythings.base.entity.Printer
xyz.elidom.print.entity.Printer
둘 다 동일 테이블 "printers" 매핑
```

**해결**:
1. `xyz.elidom.print.entity.Printer` 유지 (더 많은 기능 포함)
2. `xyz.anythings.base.entity.Printer` 삭제
3. `xyz.anythings.base.rest.PrinterController` import 수정
4. 기타 참조 수정

**영향 파일**:
- `/src/main/java/xyz/anythings/base/entity/Printer.java` - 삭제
- `/src/main/java/xyz/anythings/base/rest/PrinterController.java` - import 변경
- 기타 참조 파일들

**검증**:
```bash
grep -r "xyz.anythings.base.entity.Printer" src/main/java  # 0건
./gradlew bootRun
curl http://localhost:9500/rest/printers  # CRUD 정상 동작
```

---

### Phase 0 완료 기준
- ✅ Bean 충돌 0건
- ✅ 보안 스캔 통과 (credentials 미노출)
- ✅ 취약 라이브러리 0건
- ✅ 빌드 성공 (`./gradlew build`)
- ✅ 애플리케이션 정상 시작

---

## Phase 1: 코드 품질 개선 (P1) - 2-4주

### 목표
주석 코드 제거, 테스트 인프라 구축, @ComponentScan 최적화

---

### 1.1 주석 처리된 코드 제거 (2일)

**대상**:
- `PrintoutController.java` - 전체 주석 처리 (40줄)
- `DpcStageJobConfigUtil.java` - 100+ 라인 주석
- `MqReceiver.java` - @Component 주석

**해결**:
1. 각 파일별 사용 여부 확인
2. 미사용 시 삭제, 필요 시 주석 해제 후 테스트

**영향 파일**:
- `/src/main/java/xyz/elidom/print/rest/PrintoutController.java`
- `/src/main/java/operato/logis/dpc/service/util/DpcStageJobConfigUtil.java`
- `/src/main/java/xyz/elidom/rabbitmq/service/receiver/MqReceiver.java`

**검증**:
```bash
# 큰 주석 블록 확인
grep -r "^// *@" src/main/java --include="*.java" | wc -l  # 최소화
```

---

### 1.2 테스트 인프라 구축 (5일)

**현재 상태**: 테스트 0개, 커버리지 0%

**해결**:

1. `build.gradle`에 테스트 의존성 추가
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.junit.jupiter:junit-jupiter'
testImplementation 'org.mockito:mockito-core'
```

2. 테스트 구조 생성
```
src/test/java/
├── xyz/anythings/base/rest/
│   └── PrinterControllerTest.java
├── operato/logis/das/service/
│   └── DasAssortServiceTest.java
└── TestConfig.java
```

3. 핵심 기능 5-10개 테스트 작성
   - PrinterController CRUD
   - DasAssortService 분류 로직
   - TraceDeleteManager 삭제 로직

**영향 파일**:
- `/build.gradle`
- `/src/test/java/**/*Test.java` (신규)

**검증**:
```bash
./gradlew test
./gradlew jacocoTestReport  # 커버리지 10% 목표
```

---

### 1.3 @ComponentScan 범위 최적화 (3일)

**문제**:
```java
@ComponentScan(basePackages = {
    "xyz.anythings.*",   // 과도하게 넓음
    "xyz.elidom.*",
    "operato.*"
})
```

**해결**:
1. 실제 사용되는 패키지 분석
2. 명시적 패키지 리스트로 구체화

```java
@ComponentScan(basePackages = {
    "xyz.anythings.base",
    "xyz.anythings.gw",
    "xyz.elidom.sys",
    "xyz.elidom.orm",
    "xyz.elidom.rabbitmq",
    "xyz.elidom.mw",
    "operato.logis"
})
```

**영향 파일**:
- `/src/main/java/xyz/anythings/boot/AnythingsBootApplication.java`

**검증**:
```bash
# 시작 시간 측정 (before/after)
time ./gradlew bootRun
# Bean 로딩 로그 확인 - 20% 시간 단축 목표
```

---

### 1.4 TODO/FIXME 우선순위 처리 (5일)

**현재**: 325+ 건

**해결**:
1. 우선순위 분류
   - P0: Stub 메서드 (10건) → 구현 또는 NotImplementedException
   - P1: 버그 관련 TODO (20건) → 수정
   - P2: 나머지 → 이슈 티켓 생성

2. Stub 메서드 처리 예시:
```java
// Before
public boolean toggleLedSettingForStock(...) {
    // TODO Auto-generated method stub
    return false;  // ❌ Silent failure
}

// After
public boolean toggleLedSettingForStock(...) {
    throw new UnsupportedOperationException("Not implemented yet");
}
```

**영향 파일**:
- `/src/main/java/xyz/anythings/base/service/impl/StockService.java`
- 기타 TODO 포함 99개 파일

**검증**:
```bash
grep -r "TODO Auto-generated" src/main/java  # 0건
```

---

### Phase 1 완료 기준
- ✅ 주석 코드 90% 제거
- ✅ 테스트 커버리지 10% 달성
- ✅ 애플리케이션 시작 시간 20% 단축
- ✅ Stub 메서드 0건

---

## Phase 2: 의존성 및 구조 개선 (P2) - 1-2개월

### 목표
의존성 통일, 거대 클래스 분리, javax → jakarta 마이그레이션

---

### 2.1 JSON 라이브러리 통일 (3일)

**문제**: Jackson, Gson, json-simple, fastjson 4개 병존

**해결**:
1. Jackson으로 통일 (Spring Boot 기본)
2. Gson, json-simple 제거
3. 사용처 마이그레이션 (35개 파일)

**영향 파일**:
- `/build.gradle`
- JSON 라이브러리 사용 35개 파일

---

### 2.2 javax → jakarta 마이그레이션 (5일)

**대상**:
```java
javax.xml.bind → jakarta.xml.bind
javax.annotation → jakarta.annotation
javax.activation → jakarta.activation
```

**영향 파일**: 100+ 파일 (import 문 교체)

---

### 2.3 거대 클래스 분리 - DeviceProcessController (10일)

**문제**: 1,696줄 컨트롤러

**해결**:
1. 메서드 그룹 분석
2. Service 계층으로 로직 이동
3. Controller는 HTTP만 담당

**목표**: 500줄 이하로 축소

**영향 파일**:
- `/src/main/java/xyz/anythings/base/rest/DeviceProcessController.java`
- 신규 Service 클래스 5개

---

### 2.4 거대 클래스 분리 - DasAssortService (10일)

**문제**: 1,483줄 서비스

**해결**:
1. 책임별 분리 (분류, 검증, 실적)
2. Facade 패턴 적용

**목표**: 500줄 이하로 축소

**영향 파일**:
- `/src/main/java/operato/logis/das/service/impl/DasAssortService.java`
- 신규 Service 클래스 3-5개

---

### 2.5 XML 설정 제거 (3일)

**문제**: XML + Java Config 혼재

**해결**:
1. `application-context.xml`, `dataSource-context.xml` → Java Config 변환
2. `@ImportResource` 제거

**영향 파일**:
- `/src/main/resources/WEB-INF/*.xml` - 삭제
- `/src/main/java/xyz/anythings/boot/config/AppConfig.java` - 신규

---

### Phase 2 완료 기준
- ✅ JSON 라이브러리 1개
- ✅ javax.* import 0건
- ✅ 500줄+ 클래스 50% 감소
- ✅ XML 설정 0건
- ✅ 테스트 커버리지 25%

---

## 위험 관리

### 롤백 전략
- 각 Phase별 Git 브랜치 분리
- Phase 시작 전 Git tag 생성 (`refactor-phase0-start`)
- 실패 시 즉시 롤백 가능

### 테스트 전략
- Phase 0: 수동 통합 테스트 (Bean 충돌 확인)
- Phase 1: 자동화 테스트 구축
- Phase 2: 테스트 커버리지 기반 검증

### 배포 전략
- Phase 0: 개발 환경 → 스테이징 → 운영 (점진적)
- Phase 1-2: Canary 배포 (일부 서버부터)

---

## 검증 방법

### Phase 0 검증
```bash
# 1. 빌드 성공
./gradlew clean build

# 2. Bean 충돌 확인
./gradlew bootRun  # 오류 없이 시작되어야 함

# 3. API 동작 확인
curl http://localhost:9500/rest/printers
curl http://localhost:9500/actuator/health

# 4. 보안 스캔
grep -r "password\|secret\|key" src/main/resources/application*.properties

# 5. 취약 라이브러리 확인
./gradlew dependencies | grep -E "fastjson|groovy.*alpha"
```

### Phase 1 검증
```bash
# 1. 테스트 실행
./gradlew test

# 2. 커버리지 확인
./gradlew jacocoTestReport
# build/reports/jacoco/test/html/index.html 확인

# 3. 시작 시간 측정
time ./gradlew bootRun  # 20% 단축 확인
```

### Phase 2 검증
```bash
# 1. 의존성 확인
./gradlew dependencies | grep -E "gson|json-simple"  # 0건

# 2. javax 확인
grep -r "import javax\." src/main/java  # 0건

# 3. 클래스 크기 확인
find src/main/java -name "*.java" -exec wc -l {} \; | awk '$1 > 500'
```

---

## 타임라인

```
Week 1:    Phase 0.1-0.3 (보안, 라이브러리, PrinterController)
Week 2:    Phase 0.4-0.5 (TraceDeleteManager, Printer 엔티티)
Week 3-4:  Phase 1.1-1.2 (주석 코드, 테스트 인프라)
Week 5-6:  Phase 1.3-1.4 (ComponentScan, TODO)
Week 7-10: Phase 2.1-2.3 (의존성, DeviceProcessController)
Week 11-14: Phase 2.4-2.5 (DasAssortService, XML 제거)
```

---

## 중요 파일 목록

### Phase 0 Critical Files
- `/build.gradle` - 의존성 관리
- `/src/main/resources/application.properties` - Credentials 제거
- `/src/main/java/xyz/elidom/print/rest/PrinterController.java` - 삭제
- `/src/main/java/xyz/elidom/rabbitmq/logger/TraceDeleteManager.java` - 삭제
- `/src/main/java/xyz/anythings/base/entity/Printer.java` - 삭제

### Phase 1 Critical Files
- `/src/main/java/xyz/anythings/boot/AnythingsBootApplication.java` - ComponentScan 최적화
- `/src/test/java/` - 테스트 인프라 (신규)
- `/src/main/java/xyz/elidom/print/rest/PrintoutController.java` - 주석 처리 해결

### Phase 2 Critical Files
- `/src/main/java/xyz/anythings/base/rest/DeviceProcessController.java` - 1,696줄 → 500줄
- `/src/main/java/operato/logis/das/service/impl/DasAssortService.java` - 1,483줄 → 500줄

---

## 권장 실행 순서

1. **Phase 0부터 시작** - 런타임 안정성 확보가 최우선
2. **Phase 0.1 (보안)** 먼저 처리 - 컴플라이언스 위험
3. **Phase 1.2 (테스트)** 조기 구축 - 이후 리팩토링 안전망
4. **Phase 2 병렬 진행 가능** - 2.1-2.2 / 2.3-2.4 독립적

각 단계는 **독립 실행**, **검증 가능**, **롤백 가능**하도록 설계되었습니다.

---

## 참고 문서

- [코드 품질 분석 보고서](./code-quality-analysis.md) - 상세 코드 품질 이슈
- [아키텍처 분석 보고서](./architecture-analysis.md) - 패키지 구조 및 Bean 충돌 분석
- [의존성 분석 보고서](./dependency-analysis.md) - 빌드 설정 및 라이브러리 분석
