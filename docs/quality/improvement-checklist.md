# 개선 체크리스트

> 분석일: 2026-03-05 | 최종 수정: 2026-03-05 | 종합 점수: 6.6/10 (개선 전 5.7) | 목표: 7.5/10

---

## P0: 즉시 처리 (보안/안정성)

- [x] FastJSON 1.2.47 제거 (미사용 확인, RCE 취약점 해소) ✅ 2026-03-05
- [x] iText 4.2.2 제거, OpenPDF로 통일 (EOL) ✅ 2026-03-05
- [x] `src/test/java` 디렉토리 생성 및 테스트 프레임워크 구성 ✅ 2026-03-05
- [ ] 핵심 서비스 단위 테스트 작성 (DasAssortService, DpcPickingService)

## P1: 단기 처리 (1~2주)

### 보안
- [ ] Commons Collections 3.2.2 → 4.x 업그레이드 (otarepo-core DBIST에서 사용 중)
- [ ] 비밀번호 인코더 SHA-256 → bcrypt 전환
- [x] Deprecated 보안 설정 제거 (`security.basic.enabled` 등) ✅ 2026-03-05
- [x] Shutdown 엔드포인트 프로덕션 접근 제한 ✅ 2026-03-05

### 의존성
- [ ] Apache Velocity 1.7 → 2.3+ 업그레이드 (사용 중, otarepo-core 수정 필요)
- [ ] Commons DBCP 1.4 → HikariCP 또는 DBCP2 교체 (otarepo-core DataSourceManager에서 사용 중)
- [x] Hutool 5.7.20 제거 (미사용 확인) ✅ 2026-03-05
- [x] Groovy JSR223 알파 → 안정 버전 교체 (5.0.0-alpha-4 → 4.0.24) ✅ 2026-03-05
- [x] PostgreSQL 드라이버 버전 통일 (42.7.1 → 42.7.4) ✅ 2026-03-05
- [x] Barbecue 1.5-beta1 제거 (미사용 확인) ✅ 2026-03-05
- [x] Apache Batik 1.14 → 1.17 업그레이드 ✅ 2026-03-05
- [x] JAXB/javax.activation 제거 (미사용 확인) ✅ 2026-03-05
- [x] BOM 관리 전환 — jackson-core, aspectjweaver, gson, spring-core 버전 명시 제거 ✅ 2026-03-05
- [x] 중복 configurations.all resolutionStrategy 제거 ✅ 2026-03-05
- [x] 의존성 논리적 그룹 정리 (12개 그룹) ✅ 2026-03-05

### 설정
- [x] Deprecated Spring 설정 속성 마이그레이션 (multipart, endpoints) ✅ 2026-03-05
- [x] Docker 이미지 Java 버전 통일 (eclipse-temurin:18 → 17) ✅ 2026-03-05

## P2: 중기 처리 (1~2개월)

### 코드 품질
- [ ] DeviceProcessController 분할 (1,696줄 → 4~5개 컨트롤러)
- [ ] DasAssortService 리팩토링 (1,483줄 → 검증/분류/매핑 분리)
- [ ] 장문 메서드 (50줄 이상) 분할
- [ ] @Autowired 필드 주입 → 생성자 주입 전환 (268건)

### 테스트
- [ ] REST API 통합 테스트 작성 (OrderController 등)
- [ ] 이벤트 핸들러 단위 테스트 작성
- [ ] 테스트 커버리지 20% 달성

### 로깅
- [ ] 서비스 레이어에 SLF4J 로거 추가 (현재 14개 → 전체)
- [ ] RabbitMQ 메시지 송수신 로깅 추가
- [ ] API 응답 시간 측정 로깅 추가

## P3: 장기 처리 (3개월 이상)

### 아키텍처
- [ ] XML 설정 → 어노테이션 기반 설정 마이그레이션
- [ ] 순환 의존성 제거 (`allow-circular-references=true` 해제)
- [ ] Spring Security 6 기반 보안 설정 전면 재구성

### 문서/인프라
- [ ] API 문서 자동화 (Swagger/OpenAPI)
- [ ] Docker 취약점 스캐닝 CI/CD 통합
- [ ] 테스트 커버리지 40% 달성

---

## 점수 목표

| 카테고리 | 개선 전 | 현재 (P0 완료) | P2 완료 후 |
|----------|--------|---------------|-----------|
| 아키텍처 | 8/10 | 8/10 | 9/10 |
| 코드 품질 | 6/10 | 6/10 | 8/10 |
| 보안 | 7/10 | 9/10 | 9/10 |
| 테스트 | 1/10 | 2/10 | 5/10 |
| 유지보수성 | 6/10 | 6/10 | 8/10 |
| 문서화 | 7/10 | 7/10 | 8/10 |
| 의존성 관리 | 5/10 | 8/10 | 8/10 |
| **종합** | **5.7** | **6.6** | **7.8** |
