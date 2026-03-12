# 프론트엔드 개선 체크리스트

> 분석일: 2026-03-12 | 최종 수정: 2026-03-12 | 종합 점수: 6.6/10 | 목표: 8.0/10

---

## P0: 즉시 처리 (안정성/접근성)

- [ ] **테스트 프레임워크 구성**
  - [ ] @open-wc/testing 설치 및 설정
  - [ ] Jest 또는 Vitest 설정
  - [ ] 테스트 실행 스크립트 추가 (package.json)

- [ ] **핵심 컴포넌트 단위 테스트 작성**
  - [ ] wcs-home.js 테스트
  - [ ] das-home.js 테스트
  - [ ] bootstrap.js의 주요 함수 테스트
  - 목표: 코드 커버리지 30% 달성

- [ ] **에러 처리 강화**
  - [ ] dynamicImport() 함수에 try-catch 추가
  - [ ] 에러 발생 시 사용자 알림 표시
  - [ ] 에러 로깅 시스템 구축 (Sentry 또는 LogRocket)

- [ ] **접근성(A11y) 기본 개선**
  - [ ] 모든 이미지에 alt 속성 추가
  - [ ] 버튼에 aria-label 추가
  - [ ] 키보드 네비게이션 지원 확인

---

## P1: 단기 처리 (1~2주)

### 코드 품질

- [ ] **하드코딩 제거**
  - [ ] constants.js 파일 생성
  - [ ] 'system-home', 모듈명 등 상수화
  - [ ] 환경변수 활용 (.env 파일)

- [ ] **JSDoc 주석 추가**
  - [ ] bootstrap.js의 모든 함수
  - [ ] 동적 라우팅 관련 함수
  - [ ] 이벤트 핸들러 설명
  ```javascript
  /**
   * 동적으로 페이지 모듈을 import합니다.
   * @param {string} module - 모듈명 (metapage, system-ui, operatofill, manager)
   * @param {string} url - 페이지 경로
   * @returns {Promise<void>}
   * @throws {Error} 유효하지 않은 모듈 또는 경로
   */
  async function dynamicImport(module, url) { /* ... */ }
  ```

- [ ] **입력 검증 강화**
  - [ ] dynamicImport()에 화이트리스트 검증 추가
  - [ ] 사용자 입력 sanitization
  - [ ] URL 파라미터 검증

### 성능

- [ ] **이미지 최적화**
  - [ ] loading="lazy" 속성 추가
  - [ ] decoding="async" 속성 추가
  - [ ] WebP 포맷 변환 검토

- [ ] **번들 크기 분석**
  - [ ] webpack-bundle-analyzer 또는 rollup-plugin-visualizer 설치
  - [ ] 불필요한 Things Factory 모듈 import 제거
  - [ ] Tree shaking 최적화

### 문서화

- [ ] **컴포넌트 사용 가이드 작성**
  - [ ] 각 페이지 컴포넌트의 역할 설명
  - [ ] Mixin 사용법 문서화
  - [ ] 이벤트 흐름 다이어그램

---

## P2: 중기 처리 (1~2개월)

### TypeScript 마이그레이션

- [ ] **단계적 TypeScript 전환**
  - [ ] tsconfig.json 설정
  - [ ] bootstrap.js → bootstrap.ts 변환
  - [ ] route.js → route.ts 변환
  - [ ] 타입 정의 파일(.d.ts) 작성
  ```typescript
  // types.d.ts
  interface RouteMapping {
    id: string;
    page: string;
    template: string;
    parent?: boolean;
    routing_type: 'STATIC' | 'DYNAMIC';
    parent_id?: string;
    tagname?: string;
  }

  interface ModuleInfo {
    name: string;
    routes: RouteMapping[];
  }
  ```

- [ ] **주요 페이지 컴포넌트 TypeScript 변환**
  - [ ] wcs-home.js → wcs-home.ts
  - [ ] das-home.js → das-home.ts
  - [ ] dps-home.js → dps-home.ts

### 테스트 확대

- [ ] **E2E 테스트 구축**
  - [ ] Playwright 또는 Cypress 설정
  - [ ] 주요 사용자 시나리오 테스트
    - [ ] 로그인 → 메뉴 네비게이션 → 페이지 로딩
    - [ ] DAS 홈 → 작업 조회
    - [ ] WCS 홈 → 실적 차트
  - 목표: 주요 워크플로우 5개 E2E 테스트

- [ ] **통합 테스트 작성**
  - [ ] Redux 액션/리듀서 테스트
  - [ ] 동적 라우팅 통합 테스트
  - [ ] API 클라이언트 모킹 테스트

- [ ] **테스트 커버리지 목표**
  - [ ] 단위 테스트 커버리지 50% 달성
  - [ ] E2E 테스트 주요 워크플로우 80% 커버

### 코드 리팩토링

- [ ] **복잡한 조건 분기 단순화**
  ```javascript
  // bootstrap.js - 라우팅 필터링 로직 분리
  function filterLogisRoutes(mappings, systemMenu) { /* ... */ }
  function filterNonSystemRoutes(mappings, systemMenu) { /* ... */ }
  function isParentOrStaticRoute(mapping) { /* ... */ }
  ```

- [ ] **이벤트 기반 통신 문서화**
  - [ ] operatofill-process-start 이벤트 명세
  - [ ] operatofill-process-end 이벤트 명세
  - [ ] dynamic-page-import-request 이벤트 명세
  - [ ] 이벤트 통신 다이어그램 작성

---

## P3: 장기 처리 (3개월 이상)

### 아키텍처

- [ ] **상태 관리 개선**
  - [ ] Redux 액션/리듀서 타입 안전성 강화
  - [ ] Redux Toolkit 도입 검토
  - [ ] 비동기 상태 관리 패턴 정립 (Redux-Saga 또는 Redux-Thunk)

- [ ] **BFF(Backend for Frontend) 패턴 고려**
  - [ ] Things Factory 서버 API 활용도 높이기
  - [ ] GraphQL 도입 검토
  - [ ] 서버사이드 데이터 aggregation

### 성능 최적화

- [ ] **번들 최적화**
  - [ ] Code splitting 전략 수립
  - [ ] Dynamic import 범위 확대
  - [ ] 초기 로딩 시간 50% 단축 목표

- [ ] **성능 모니터링**
  - [ ] Web Vitals 추적 (LCP, FID, CLS)
  - [ ] Lighthouse CI 통합
  - [ ] 성능 예산(Performance Budget) 설정
  - 목표: Lighthouse 점수 90점 이상

- [ ] **메모이제이션 전략**
  - [ ] Lit의 hasChanged 활용
  - [ ] 반복 계산 결과 캐싱
  - [ ] 컴포넌트 렌더링 최적화

### 접근성(A11y) 완성

- [ ] **WCAG 2.1 AA 레벨 준수**
  - [ ] 스크린 리더 테스트
  - [ ] 키보드 전용 네비게이션
  - [ ] 색상 대비 4.5:1 이상
  - [ ] Focus 인디케이터 명확화

- [ ] **A11y 자동화 테스트**
  - [ ] axe-core 통합
  - [ ] CI/CD에 접근성 테스트 추가

### 문서화 및 도구

- [ ] **Storybook 도입**
  - [ ] 컴포넌트 카탈로그 구축
  - [ ] Props/Events 문서 자동 생성
  - [ ] 인터랙티브 컴포넌트 테스트

- [ ] **개발자 가이드 작성**
  - [ ] Things Factory 프레임워크 활용 가이드
  - [ ] 새로운 페이지 추가 방법
  - [ ] Mixin 작성 가이드
  - [ ] 배포 가이드

- [ ] **CI/CD 파이프라인**
  - [ ] GitHub Actions 워크플로우 구성
  - [ ] 자동 테스트 실행
  - [ ] 린트 검사 (ESLint, Prettier)
  - [ ] 자동 배포 (Netlify, Vercel, 또는 자체 서버)

---

## 점수 목표

| 카테고리 | 현재 (03-12) | P0 완료 후 | P1 완료 후 | P2 완료 후 | P3 완료 후 |
|---------|-------------|-----------|-----------|-----------|-----------|
| 코드 구조 | 8.5/10 | 8.5/10 | 8.5/10 | 9.0/10 | 9.5/10 |
| 가독성 | 7.0/10 | 7.0/10 | 8.0/10 | 8.5/10 | 9.0/10 |
| 유지보수성 | 7.5/10 | 7.5/10 | 8.0/10 | 8.5/10 | 9.0/10 |
| 재사용성 | 8.0/10 | 8.0/10 | 8.0/10 | 8.5/10 | 9.0/10 |
| **테스트** | **2.0/10** | **4.0/10** | **5.0/10** | **7.0/10** | **8.5/10** |
| 보안 | 7.0/10 | 8.0/10 | 8.5/10 | 9.0/10 | 9.0/10 |
| 성능 | 7.5/10 | 7.5/10 | 8.0/10 | 8.5/10 | 9.0/10 |
| 접근성 | 5.0/10 | 6.5/10 | 7.0/10 | 7.5/10 | 9.0/10 |
| **종합** | **6.6** | **7.1** | **7.6** | **8.3** | **9.0** |

---

## 우선순위 기준

**P0 (즉시 처리)**:
- 안정성 및 접근성 기본 요건
- 사용자 경험에 직접 영향
- 법적 요구사항(접근성)

**P1 (단기 처리)**:
- 코드 품질 기본기
- 개발 생산성 향상
- 보안 강화

**P2 (중기 처리)**:
- 타입 안전성 확보
- 테스트 확대
- 리팩토링

**P3 (장기 처리)**:
- 아키텍처 개선
- 성능 최적화
- 도구 및 자동화

---

## 빠른 성과(Quick Win) 항목

다음 항목들은 **2~3일 내 완료 가능**하며 즉각적인 품질 개선 효과가 있습니다:

1. ✅ **모든 이미지에 alt 속성 추가** (30분)
2. ✅ **loading="lazy" 속성 추가** (15분)
3. ✅ **constants.js 파일 생성 및 하드코딩 제거** (2시간)
4. ✅ **dynamicImport() 에러 처리 추가** (1시간)
5. ✅ **주요 함수 JSDoc 주석 추가** (4시간)

**권장**: P0 작업 전에 Quick Win 항목부터 처리하여 빠른 성과 확보

---

## 참고 자료

### 테스트
- [Open WC Testing](https://open-wc.org/docs/testing/testing-package/)
- [Lit Testing Guide](https://lit.dev/docs/tools/testing/)
- [Playwright](https://playwright.dev/)

### TypeScript
- [Lit TypeScript Guide](https://lit.dev/docs/components/typescript/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html)

### 접근성
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [axe DevTools](https://www.deque.com/axe/devtools/)
- [A11y Project](https://www.a11yproject.com/)

### Things Factory
- [Things Factory Documentation](https://things-factory.net/)
- [Lit Element](https://lit.dev/)
